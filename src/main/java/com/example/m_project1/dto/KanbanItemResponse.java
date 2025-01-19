package com.example.m_project1.dto;

import com.example.m_project1.entity.KanbanItem;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class KanbanItemResponse {
    private Long id;
    private String title;
    private String description;
    private Long assigneeId;
    private String assigneeEmail;
    private Integer priority;
    private KanbanItem.Status status;
    private Long taskId;
    private String taskTitle;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public KanbanItemResponse(KanbanItem item) {
        this.id = item.getId();
        this.title = item.getTitle();
        this.description = item.getDescription();
        if (item.getAssignee() != null) {
            this.assigneeId = item.getAssignee().getUserId();
            this.assigneeEmail = item.getAssignee().getEmail();
        }
        this.priority = item.getPriority();
        this.status = item.getStatus();
        if (item.getRelatedTask() != null) {
            this.taskId = item.getRelatedTask().getId();
            this.taskTitle = item.getRelatedTask().getTitle();
        }
        this.createdAt = item.getCreatedAt();
        this.updatedAt = item.getUpdatedAt();
    }
}