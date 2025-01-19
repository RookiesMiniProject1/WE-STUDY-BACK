package com.example.m_project1.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "chat_message")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User sender;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public enum MessageType {
        CHAT("일반 메시지"),
        JOIN("입장"),
        LEAVE("퇴장"),
        NOTICE("공지");

        MessageType(String description) {
        }
    }

    @Builder
    public ChatMessage(ChatRoom chatRoom, User sender, String content, MessageType type) {
        this.chatRoom = Objects.requireNonNull(chatRoom, "채팅방은 필수입니다.");
        this.sender = Objects.requireNonNull(sender, "발신자는 필수입니다.");
        this.content = Objects.requireNonNull(content, "메시지 내용은 필수입니다.");
        this.type = Objects.requireNonNull(type, "메시지 타입은 필수입니다.");
        this.timestamp = LocalDateTime.now();

        validateMessage(chatRoom, sender);
    }

    private void validateMessage(ChatRoom chatRoom, User sender) {
        if (!chatRoom.isMember(sender)) {
            throw new IllegalStateException("채팅방 멤버만 메시지를 보낼 수 있습니다.");
        }
    }
}