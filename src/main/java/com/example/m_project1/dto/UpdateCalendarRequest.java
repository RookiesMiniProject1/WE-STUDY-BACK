package com.example.m_project1.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class UpdateCalendarRequest {
    @Size(min = 2, max = 100, message = "제목은 2자 이상 100자 이하여야 합니다")
    private String title;

    @Size(max = 1000, message = "내용은 1000자 이하여야 합니다")
    private String content;

    @Future(message = "시작일은 현재 시간 이후여야 합니다")
    private LocalDateTime startDate;

    @Future(message = "종료일은 현재 시간 이후여야 합니다")
    private LocalDateTime endDate;

    private Boolean isCompleted;
}
