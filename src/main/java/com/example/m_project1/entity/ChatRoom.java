package com.example.m_project1.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "chat_room")
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String roomName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private StudyGroup studyGroup;

    @Column(nullable = false)
    private boolean isDefault;  // 그룹 기본 채팅방 여부

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastMessageAt;

    // 새로운 필드 추가
    @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private int participantCount;

    // 기본 채팅방 생성을 위한 정적 팩토리 메서드
    public static ChatRoom createDefaultRoom(StudyGroup studyGroup) {
        return ChatRoom.builder()
                .roomName(studyGroup.getTitle() + " 공식 채팅방")
                .studyGroup(studyGroup)
                .isDefault(true)
                .build();
    }

    // 추가 채팅방 생성을 위한 정적 팩토리 메서드
    public static ChatRoom createAdditionalRoom(StudyGroup studyGroup, String roomName) {
        return ChatRoom.builder()
                .roomName(roomName)
                .studyGroup(studyGroup)
                .isDefault(false)
                .build();
    }

    @Builder
    private ChatRoom(String roomName, StudyGroup studyGroup, boolean isDefault) {
        this.roomName = Objects.requireNonNull(roomName, "채팅방 이름은 필수입니다.");
        this.studyGroup = Objects.requireNonNull(studyGroup, "스터디 그룹은 필수입니다.");
        this.isDefault = isDefault;
        this.createdAt = LocalDateTime.now();
        this.lastMessageAt = this.createdAt;
        this.participantCount = 0;  // 기본값 설정
    }

    public void updateLastMessageTime() {
        this.lastMessageAt = LocalDateTime.now();
    }

    public boolean isMember(User user) {
        return studyGroup.isMember(user);
    }

    public boolean canManage(User user) {
        return studyGroup.isLeader(user) || user.equals(studyGroup.getMentor());
    }

    // 기본 채팅방은 삭제 불가
    public boolean canDelete(User user) {
        return !isDefault && canManage(user);
    }

    public void incrementParticipantCount() {
        this.participantCount++;
    }

    public void decrementParticipantCount() {
        if (this.participantCount > 0) {
            this.participantCount--;
        }
    }

}

