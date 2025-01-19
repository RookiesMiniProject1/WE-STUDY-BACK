package com.example.m_project1.Service;

import com.example.m_project1.dto.ChatMessageDto;
import com.example.m_project1.entity.ChatMessage;
import com.example.m_project1.entity.ChatRoom;
import com.example.m_project1.entity.StudyGroup;
import com.example.m_project1.entity.User;
import com.example.m_project1.exception.AccessDeniedException;
import com.example.m_project1.exception.ResourceNotFoundException;
import com.example.m_project1.repository.ChatMessageRepository;
import com.example.m_project1.repository.ChatRoomRepository;
import com.example.m_project1.repository.StudyGroupRepository;
import com.example.m_project1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatRoom createChatRoom(Long groupId, Long userId, String roomName) {
        // 스터디 그룹 확인
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("스터디 그룹을 찾을 수 없습니다."));

        // 사용자 확인
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        // 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .roomName(roomName)
                .studyGroup(group)
                .isDefault(false)
                .build();

        // 생성자 자동 참가 처리
        chatRoom.incrementParticipantCount();

        return chatRoomRepository.save(chatRoom);
    }

    @Transactional
    public void addParticipant(Long roomId, Long userId) {
        // 채팅방 확인
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("채팅방을 찾을 수 없습니다."));

        // 사용자 확인
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        // 참가자 수 증가
        chatRoom.incrementParticipantCount();
    }

    @Transactional
    public void removeParticipant(Long roomId, Long userId) {
        // 채팅방 확인
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("채팅방을 찾을 수 없습니다."));

        // 사용자 확인
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        // 참가자 수 감소
        chatRoom.decrementParticipantCount();
    }


    @Transactional
    public void deleteChatRoom(Long roomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("채팅방을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        StudyGroup group = chatRoom.getStudyGroup();
        group.deleteChatRoom(chatRoom, user);
        chatRoomRepository.delete(chatRoom);
    }

    @Transactional
    public ChatMessage saveMessage(Long roomId, Long userId, ChatMessageDto messageDto) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("채팅방을 찾을 수 없습니다."));

        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        if (!chatRoom.isMember(sender)) {
            throw new AccessDeniedException("채팅방에 참여할 권한이 없습니다.");
        }

        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .content(messageDto.getContent())
                .type(messageDto.getType())
                .build();

        chatRoom.updateLastMessageTime();
        return chatMessageRepository.save(message);
    }

    public List<ChatRoom> getGroupChatRooms(Long groupId, Long userId) {
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("스터디 그룹을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        if (!group.isMember(user)) {
            throw new AccessDeniedException("그룹의 멤버만 채팅방 목록을 조회할 수 있습니다.");
        }

        return group.getAllChatRooms();
    }

    public List<ChatMessageDto> getAllMessages(Long roomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("채팅방을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        if (!chatRoom.isMember(user)) {
            throw new AccessDeniedException("채팅방에 참여할 권한이 없습니다.");
        }

        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderByTimestampAsc(roomId);
        return messages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<ChatMessageDto> getRecentMessages(Long roomId, Long userId, int limit) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("채팅방을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        if (!chatRoom.isMember(user)) {
            throw new AccessDeniedException("채팅방에 참여할 권한이 없습니다.");
        }
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderByTimestampDesc(roomId, pageable);
        return messages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ChatMessageDto convertToDto(ChatMessage message) {
        return ChatMessageDto.builder()
                .id(message.getId())
                .roomId(message.getChatRoom().getId())
                .senderId(message.getSender().getUserId())
                .senderName(message.getSender().getEmail())  // 또는 다른 식별 가능한 정보
                .content(message.getContent())
                .type(message.getType())
                .timestamp(message.getTimestamp())
                .build();
    }
}




