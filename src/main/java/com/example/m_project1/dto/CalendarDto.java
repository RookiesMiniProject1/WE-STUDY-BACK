package com.example.m_project1.dto;

import com.example.m_project1.entity.Calendar;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class CalendarDto {

    @Getter
    @NoArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "제목은 필수입니다")
        private String title;
        private String content;
        @NotNull(message = "시작 일시는 필수입니다")
        private LocalDateTime startDate;
        @NotNull(message = "종료 일시는 필수입니다")
        private LocalDateTime endDate;
        private Long groupId;
        private Long taskId;
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateRequest {
        private String title;
        private String content;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Boolean isCompleted;
    }

    @Getter
    @Builder
    public static class Response {
        private Long id;
        private String title;
        private String content;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String type;
        private Boolean isCompleted;
        private Long userId;
        private String userEmail;
        private Long groupId;
        private Long taskId;
        private LocalDateTime createdAt;

        public static Response from(Calendar calendar) {
            return Response.builder()
                    .id(calendar.getId())
                    .title(calendar.getTitle())
                    .content(calendar.getContent())
                    .startDate(calendar.getStartDate())
                    .endDate(calendar.getEndDate())
                    .type(calendar.getType().name())
                    .isCompleted(calendar.isCompleted())
                    .userId(calendar.getUser().getUserId())
                    .userEmail(calendar.getUser().getEmail())
                    .groupId(calendar.getStudyGroup() != null ? calendar.getStudyGroup().getId() : null)
                    .taskId(calendar.getTask() != null ? calendar.getTask().getId() : null)
                    .createdAt(calendar.getCreatedAt())
                    .build();
        }
    }
}