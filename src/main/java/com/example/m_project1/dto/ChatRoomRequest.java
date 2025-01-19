package com.example.m_project1.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChatRoomRequest {
    private String roomName;
    private Long groupId;
}
