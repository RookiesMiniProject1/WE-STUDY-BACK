package com.example.m_project1.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class ChatRoomParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private LocalDateTime joinedAt;
    private LocalDateTime lastReadAt;
}

