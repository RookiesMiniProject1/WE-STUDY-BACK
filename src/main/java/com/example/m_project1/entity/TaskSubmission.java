package com.example.m_project1.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.example.m_project1.entity.Task;  // 올바른 import

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "task_submission")
public class TaskSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.SUBMITTED;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    private LocalDateTime updatedAt;

    private String filePath;
    private String fileName;

    @Column(columnDefinition = "TEXT")
    private String mentorFeedback;

    @Min(0) @Max(100)
    private Integer mentorScore;

    @Column(columnDefinition = "TEXT")
    private String peerFeedback;

    @Min(0) @Max(5)
    private Integer peerScore;

    public enum Status {
        SUBMITTED("제출됨"),
        REVIEWED("검토완료");

        private final String description;

        Status(String description) {
            this.description = description;
        }
    }

    @Builder
    public TaskSubmission(Task task, User user, String content, String filePath, String fileName) {
        this.task = Objects.requireNonNull(task, "과제는 필수입니다.");
        this.user = Objects.requireNonNull(user, "제출자는 필수입니다.");
        this.content = Objects.requireNonNull(content, "내용은 필수입니다.");
        this.filePath = filePath;
        this.fileName = fileName;
        this.submittedAt = LocalDateTime.now();
        this.updatedAt = this.submittedAt;

        validateSubmission(task, user);
    }

    private void validateSubmission(Task task, User user) {
        if (task.isOverdue()) {
            throw new IllegalStateException("제출 기한이 지났습니다.");
        }
        if (!task.getStudyGroup().isMember(user)) {
            throw new IllegalStateException("해당 그룹의 멤버만 과제를 제출할 수 있습니다.");
        }
    }

    public void update(String content, String filePath, String fileName) {
        this.content = Objects.requireNonNull(content, "내용은 필수입니다.");
        this.filePath = filePath;
        this.fileName = fileName;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateMentorFeedback(String feedback, Integer score) {
        this.mentorFeedback = Objects.requireNonNull(feedback, "피드백 내용은 필수입니다.");
        if (score != null) {
            if (score < 0 || score > 100) {
                throw new IllegalArgumentException("점수는 0에서 100 사이여야 합니다.");
            }
            this.mentorScore = score;
        }
        this.status = Status.REVIEWED;
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePeerFeedback(String feedback, Integer score) {
        this.peerFeedback = Objects.requireNonNull(feedback, "피드백 내용은 필수입니다.");
        if (score != null) {
            if (score < 0 || score > 5) {
                throw new IllegalArgumentException("점수는 0에서 5 사이여야 합니다.");
            }
            this.peerScore = score;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public boolean canReview(User reviewer) {
        StudyGroup group = task.getStudyGroup();
        return group.getMentor().equals(reviewer) ||
                (group.isMember(reviewer) && !this.user.equals(reviewer));
    }
}