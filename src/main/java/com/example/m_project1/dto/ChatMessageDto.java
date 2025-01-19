package com.example.m_project1.dto;

import com.example.m_project1.entity.ChatMessage;
import lombok.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ChatMessageDto {
    private Long id;
    private Long roomId;
    private Long senderId;
    private String senderName;
    private String content;
    @NotNull(message = "메시지 타입은 필수입니다")
    private ChatMessage.MessageType type;
    private LocalDateTime timestamp;

    @NotNull(message = "메시지 내용은 필수입니다")
    @Size(min = 1, max = 1000, message = "메시지는 1자 이상 1000자 이하여야 합니다")
    public String getContent() {
        return content;
    }
}
