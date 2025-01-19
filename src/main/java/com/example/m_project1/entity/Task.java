package com.example.m_project1.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "task")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private StudyGroup studyGroup;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.TODO;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskSubmission> submissions = new ArrayList<>();

    @OneToMany(mappedBy = "relatedTask", cascade = CascadeType.ALL)
    private List<KanbanItem> kanbanItems = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Calendar> schedules = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum Status {
        TODO("할 일"),
        IN_PROGRESS("진행 중"),
        COMPLETED("완료됨");

        private final String description;

        Status(String description) {
            this.description = description;
        }
    }

    @Builder
    public Task(StudyGroup studyGroup, String title, String description, LocalDateTime deadline) {
        this.studyGroup = Objects.requireNonNull(studyGroup, "스터디 그룹은 필수입니다.");
        this.title = Objects.requireNonNull(title, "제목은 필수입니다.");
        this.description = description;
        this.deadline = Objects.requireNonNull(deadline, "마감일은 필수입니다.");

        if (deadline.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("마감일은 현재 시간 이후여야 합니다.");
        }
    }

    public void update(String title, String description, LocalDateTime deadline) {
        this.title = Objects.requireNonNull(title, "제목은 필수입니다.");
        this.description = description;

        if (deadline != null) {
            if (deadline.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("마감일은 현재 시간 이후여야 합니다.");
            }
            this.deadline = deadline;
        }

        this.updatedAt = LocalDateTime.now();
    }

    public void updateStatus(Status newStatus) {
        validateStatusTransition(this.status, newStatus);
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    private void validateStatusTransition(Status current, Status next) {
        if (current == Status.COMPLETED) {
            throw new IllegalStateException("완료된 과제의 상태는 변경할 수 없습니다.");
        }
        if (current == Status.TODO && next == Status.COMPLETED) {
            throw new IllegalStateException("진행 중 상태를 거치지 않고 완료할 수 없습니다.");
        }
    }

    public boolean isOverdue() {
        return LocalDateTime.now().isAfter(deadline);
    }

    public boolean isSubmitted(User user) {
        return submissions.stream()
                .anyMatch(submission -> submission.getUser().equals(user));
    }

    public boolean canModify(User user) {
        return user.equals(studyGroup.getMentor());
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}