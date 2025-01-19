package com.example.m_project1.dto;

import com.example.m_project1.entity.KanbanItem;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class KanbanItemDto {

    @Getter
    @NoArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "제목은 필수입니다")
        private String title;

        private String description;

        @NotNull(message = "우선순위는 필수입니다")
        @Min(value = 1, message = "우선순위는 1-3 사이여야 합니다")
        @Max(value = 3, message = "우선순위는 1-3 사이여야 합니다")
        private Integer priority;

        private Long assigneeId;
        private Long taskId;  // optional
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateRequest {
        private String title;
        private String description;
        private Integer priority;
        private KanbanItem.Status status;
        private Long assigneeId;
    }

    @Getter
    @NoArgsConstructor
    public static class Response {
        private Long id;
        private String title;
        private String description;
        private Long assigneeId;
        private String assigneeName;
        private Integer priority;
        private String status;
        private Long taskId;
        private String taskTitle;
        private Long groupId;

        public Response(KanbanItem item) {
            this.id = item.getId();
            this.title = item.getTitle();
            this.description = item.getDescription();
            this.assigneeId = item.getAssignee() != null ? item.getAssignee().getUserId() : null;
            this.assigneeName = item.getAssignee() != null ? item.getAssignee().getEmail() : null;
            this.priority = item.getPriority();
            this.status = item.getStatus().name();
            this.taskId = item.getRelatedTask() != null ? item.getRelatedTask().getId() : null;
            this.taskTitle = item.getRelatedTask() != null ? item.getRelatedTask().getTitle() : null;
            this.groupId = item.getStudyGroup().getId();
        }
    }
}