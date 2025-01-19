package com.example.m_project1.controller;

import com.example.m_project1.dto.*;
import com.example.m_project1.entity.ChatMessage;
import com.example.m_project1.entity.ChatRoom;
import com.example.m_project1.Service.ChatService;
import com.example.m_project1.entity.User;
import com.example.m_project1.exception.InvalidOperationException;
import com.example.m_project1.exception.ResourceNotFoundException;
import com.example.m_project1.repository.UserRepository;
import com.example.m_project1.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/api/groups/{groupId}/chat/rooms")
    public ResponseEntity<?> createChatRoom(
            @PathVariable Long groupId,
            @Valid @RequestBody CreateChatRoomRequest request,
            Authentication authentication) {
        try {
            Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
            ChatRoom chatRoom = chatService.createChatRoom(groupId, userId, request.getRoomName());
            messagingTemplate.convertAndSend("/topic/new-room", new ChatRoomResponse(chatRoom));
            return ResponseEntity.ok(new ChatRoomResponse(chatRoom));
        } catch (Exception e) {
            log.error("채팅방 생성 중 오류 발생: ", e);
            throw new InvalidOperationException(e.getMessage());
        }
    }

    @GetMapping("/api/groups/{groupId}/chat/rooms")
    public ResponseEntity<?> getGroupChatRooms(
            @PathVariable Long groupId,
            Authentication authentication) {
        Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        List<ChatRoom> rooms = chatService.getGroupChatRooms(groupId, userId);
        List<ChatRoomResponse> response = rooms.stream()
                .map(ChatRoomResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/groups/{groupId}/chat/rooms/{roomId}")
    public ResponseEntity<?> deleteChatRoom(
            @PathVariable Long groupId,
            @PathVariable Long roomId,
            Authentication authentication) {
        Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        chatService.deleteChatRoom(roomId, userId);
        return ResponseEntity.ok().build();
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDto chatMessage, Principal principal) {
        try {
            // 발신자 정보 확인
            User sender = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

            // senderName이 null 또는 빈 값인 경우, Principal에서 이메일 설정
            if (chatMessage.getSenderName() == null || chatMessage.getSenderName().isEmpty()) {
                chatMessage.setSenderName(sender.getEmail());
            }

            // 메시지 저장
            ChatMessage savedMessage = chatService.saveMessage(
                    chatMessage.getRoomId(),
                    sender.getUserId(),
                    chatMessage
            );

            // 저장된 메시지를 구독자들에게 전송
            messagingTemplate.convertAndSend(
                    "/topic/room." + chatMessage.getRoomId(),
                    convertToDto(savedMessage)
            );

        } catch (Exception e) {
            log.error("메시지 전송 중 오류 발생: ", e);
            throw new InvalidOperationException(e.getMessage());
        }
        log.info("전송된 메시지: {}", chatMessage.getSenderName());
    }



    @MessageMapping("/chat.join")
    public void joinChat(@Payload ChatMessageDto chatMessage, Principal principal) {
        Long roomId = chatMessage.getRoomId();

        // senderName이 없으면 Principal에서 이메일을 설정
        if (chatMessage.getSenderName() == null || chatMessage.getSenderName().isEmpty()) {
            User user = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
            chatMessage.setSenderName(user.getEmail());
        }

        chatMessage.setType(ChatMessage.MessageType.JOIN);
        messagingTemplate.convertAndSend(
                "/topic/room." + roomId,
                chatMessage
        );
    }


    @MessageMapping("/chat.leave")
    public void leaveChat(@Payload ChatMessageDto chatMessage, Principal principal) {
        Long roomId = chatMessage.getRoomId();
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        chatService.removeParticipant(roomId, user.getUserId());  // 참가자 제거

        chatMessage.setType(ChatMessage.MessageType.LEAVE);
        messagingTemplate.convertAndSend(
                "/topic/room." + roomId,
                chatMessage
        );
    }


    @GetMapping("/api/groups/{groupId}/chat/rooms/{roomId}/messages")
    public ResponseEntity<?> getChatMessages(
            @PathVariable Long groupId,
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "50") int limit,
            Authentication authentication) {
        Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        List<ChatMessageDto> messages = chatService.getRecentMessages(roomId, userId, limit);
        return ResponseEntity.ok(messages);
    }

    private ChatMessageDto convertToDto(ChatMessage message) {
        return ChatMessageDto.builder()
                .id(message.getId())
                .roomId(message.getChatRoom().getId())
                .senderId(message.getSender().getUserId())
                .senderName(message.getSender().getEmail())
                .content(message.getContent())
                .type(message.getType())
                .timestamp(message.getTimestamp())
                .build();
    }
}
