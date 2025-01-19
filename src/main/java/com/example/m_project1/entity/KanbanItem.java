package com.example.m_project1.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "kanban_item")
public class KanbanItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kanban_board_id", nullable = false)
    private KanbanBoard kanbanBoard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private StudyGroup studyGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @Column(nullable = false)
    private Integer priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.TODO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task relatedTask;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public enum Status {
        TODO("할 일"),
        IN_PROGRESS("진행 중"),
        DONE("완료");

        private final String description;

        Status(String description) {
            this.description = description;
        }
    }

    @Builder
    public KanbanItem(String title, String description, KanbanBoard kanbanBoard,
                      StudyGroup studyGroup, User assignee, Integer priority, Task relatedTask) {
        this.title = Objects.requireNonNull(title, "제목은 필수입니다.");
        this.description = description;
        this.kanbanBoard = Objects.requireNonNull(kanbanBoard, "칸반 보드는 필수입니다.");
        this.studyGroup = Objects.requireNonNull(studyGroup, "스터디 그룹은 필수입니다.");
        this.assignee = assignee;
        this.priority = Objects.requireNonNull(priority, "우선순위는 필수입니다.");
        this.relatedTask = relatedTask;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;

        validatePriority(priority);
    }

    private void validatePriority(Integer priority) {
        if (priority < 1 || priority > 3) {
            throw new IllegalArgumentException("우선순위는 1~3 사이여야 합니다.");
        }
    }

    public void update(String title, String description, User assignee,
                       Integer priority, Status status) {
        if (title != null) {
            this.title = title;
        }
        this.description = description;
        this.assignee = assignee;
        if (priority != null) {
            validatePriority(priority);
            this.priority = priority;
        }
        if (status != null) {
            this.status = status;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public boolean canModify(User user) {
        return studyGroup.isLeader(user) ||
                user.equals(studyGroup.getMentor()) ||
                (assignee != null && assignee.equals(user));
    }

    public void updateStatus(Status newStatus) {
        this.status = Objects.requireNonNull(newStatus, "상태는 필수입니다.");
        this.updatedAt = LocalDateTime.now();
    }
}