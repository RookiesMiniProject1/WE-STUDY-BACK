package com.example.m_project1.dto;

import com.example.m_project1.entity.Calendar;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CalendarResponse {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Calendar.ScheduleType type;
    private boolean isCompleted;
    private Long userId;
    private String userEmail;
    private Long groupId;
    private String groupTitle;
    private Long taskId;
    private String taskTitle;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CalendarResponse(Calendar calendar) {
        this.id = calendar.getId();
        this.title = calendar.getTitle();
        this.content = calendar.getContent();
        this.startDate = calendar.getStartDate();
        this.endDate = calendar.getEndDate();
        this.type = calendar.getType();
        this.isCompleted = calendar.isCompleted();
        this.userId = calendar.getUser().getUserId();
        this.userEmail = calendar.getUser().getEmail();

        if (calendar.getStudyGroup() != null) {
            this.groupId = calendar.getStudyGroup().getId();
            this.groupTitle = calendar.getStudyGroup().getTitle();
        }

        if (calendar.getTask() != null) {
            this.taskId = calendar.getTask().getId();
            this.taskTitle = calendar.getTask().getTitle();
        }

        this.createdAt = calendar.getCreatedAt();
        this.updatedAt = calendar.getUpdatedAt();
    }
}
