package com.example.m_project1.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TypingStatusDto {
    private Long roomId;
    private Long userId;
    private boolean isTyping;
}