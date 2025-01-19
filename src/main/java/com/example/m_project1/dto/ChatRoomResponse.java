package com.example.m_project1.dto;

import com.example.m_project1.entity.ChatRoom;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ChatRoomResponse {
    private Long id;
    private String roomName;
    private Long groupId;
    private String groupName;
    private boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;
    private int messageCount;
    private int participantCount;

    public ChatRoomResponse(ChatRoom chatRoom) {
        this.id = chatRoom.getId();
        this.roomName = chatRoom.getRoomName();
        this.groupId = chatRoom.getStudyGroup().getId();
        this.groupName = chatRoom.getStudyGroup().getTitle();
        this.isDefault = chatRoom.isDefault();
        this.createdAt = chatRoom.getCreatedAt();
        this.lastMessageAt = chatRoom.getLastMessageAt();
        this.messageCount = chatRoom.getMessages().size();
        this.participantCount = chatRoom.getParticipantCount();
    }
}
