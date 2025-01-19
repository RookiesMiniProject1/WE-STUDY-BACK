package com.example.m_project1.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CreateCalendarRequest {
    @NotBlank(message = "제목은 필수입니다")
    @Size(min = 2, max = 100, message = "제목은 2자 이상 100자 이하여야 합니다")
    private String title;

    @Size(max = 1000, message = "내용은 1000자 이하여야 합니다")
    private String content;

    @NotNull(message = "시작일은 필수입니다")
    @Future(message = "시작일은 현재 시간 이후여야 합니다")
    private LocalDateTime startDate;

    @NotNull(message = "종료일은 필수입니다")
    @Future(message = "종료일은 현재 시간 이후여야 합니다")
    private LocalDateTime endDate;

    private Long groupId;
    private Long taskId;
}
