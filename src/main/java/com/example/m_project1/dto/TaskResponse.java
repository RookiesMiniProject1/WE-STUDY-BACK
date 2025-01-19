package com.example.m_project1.dto;

import com.example.m_project1.entity.Task;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private Task.Status status;
    private Long groupId;
    private String groupTitle;
    private Long mentorId;
    private String mentorEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TaskSubmissionResponse> submissions;
    private boolean isOverdue;

    public TaskResponse(Task task) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.deadline = task.getDeadline();
        this.status = task.getStatus();
        this.groupId = task.getStudyGroup().getId();
        this.groupTitle = task.getStudyGroup().getTitle();
        this.mentorId = task.getStudyGroup().getMentor().getUserId();
        this.mentorEmail = task.getStudyGroup().getMentor().getEmail();
        this.createdAt = task.getCreatedAt();
        this.updatedAt = task.getUpdatedAt();
        this.submissions = task.getSubmissions().stream()
                .map(TaskSubmissionResponse::new)
                .collect(Collectors.toList());
        this.isOverdue = task.isOverdue();
    }
}