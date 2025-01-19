package com.example.m_project1.dto;

import com.example.m_project1.entity.TaskSubmission;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class TaskSubmissionResponse {
    private Long id;
    private Long taskId;
    private Long userId;
    private String userEmail;
    private String content;
    private String filePath;
    private String fileName;
    private TaskSubmission.Status status;
    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;
    private String mentorFeedback;
    private Integer mentorScore;
    private String peerFeedback;
    private Integer peerScore;

    public TaskSubmissionResponse(TaskSubmission submission) {
        this.id = submission.getId();
        this.taskId = submission.getTask().getId();
        this.userId = submission.getUser().getUserId();
        this.userEmail = submission.getUser().getEmail();
        this.content = submission.getContent();
        this.filePath = submission.getFilePath();
        this.fileName = submission.getFileName();
        this.status = submission.getStatus();
        this.submittedAt = submission.getSubmittedAt();
        this.updatedAt = submission.getUpdatedAt();
        this.mentorFeedback = submission.getMentorFeedback();
        this.mentorScore = submission.getMentorScore();
        this.peerFeedback = submission.getPeerFeedback();
        this.peerScore = submission.getPeerScore();
    }
}
