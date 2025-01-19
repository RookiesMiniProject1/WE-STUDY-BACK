package com.example.m_project1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateChatRoomRequest {
    @NotBlank(message = "채팅방 이름은 필수입니다")
    @Size(min = 2, max = 50, message = "채팅방 이름은 2자 이상 50자 이하여야 합니다")
    private String roomName;
}
