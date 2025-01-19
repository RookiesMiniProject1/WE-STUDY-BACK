package com.example.m_project1.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Setter
@Getter
@NoArgsConstructor
@Table(name = "calendar")
public class Calendar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private StudyGroup studyGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleType type;

    @Column(nullable = false)
    private boolean isCompleted = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;




    public enum ScheduleType {
        PERSONAL("개인 일정"),
        GROUP("그룹 일정"),
        TASK("과제 일정");

        private final String description;

        ScheduleType(String description) {
            this.description = description;
        }
    }

    @Builder
    public Calendar(User user, StudyGroup studyGroup, Task task, String title,
                    String content, LocalDateTime startDate, LocalDateTime endDate,
                    ScheduleType type) {
        this.user = Objects.requireNonNull(user, "사용자는 필수입니다.");
        this.title = Objects.requireNonNull(title, "제목은 필수입니다.");
        this.startDate = Objects.requireNonNull(startDate, "시작일은 필수입니다.");
        this.endDate = Objects.requireNonNull(endDate, "종료일은 필수입니다.");
        this.type = Objects.requireNonNull(type, "일정 유형은 필수입니다.");

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("종료일은 시작일 이후여야 합니다.");
        }

        // GROUP이나 TASK 타입인 경우 그룹 정보 필수
        if ((type == ScheduleType.GROUP || type == ScheduleType.TASK) && studyGroup == null) {
            throw new IllegalArgumentException("그룹 일정에는 그룹 정보가 필요합니다.");
        }

        // TASK 타입인 경우 과제 정보 필수
        if (type == ScheduleType.TASK && task == null) {
            throw new IllegalArgumentException("과제 일정에는 과제 정보가 필요합니다.");
        }

        this.studyGroup = studyGroup;
        this.task = task;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public void update(String title, String content, LocalDateTime startDate,
                       LocalDateTime endDate) {
        if (title != null) {
            this.title = title;
        }
        this.content = content;
        if (startDate != null) {
            this.startDate = startDate;
        }
        if (endDate != null) {
            if (endDate.isBefore(this.startDate)) {
                throw new IllegalArgumentException("종료일은 시작일 이후여야 합니다.");
            }
            this.endDate = endDate;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void complete() {
        this.isCompleted = true;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean canModify(User user) {
        return this.user.equals(user) ||
                (this.studyGroup != null &&
                        (this.studyGroup.isLeader(user) ||
                                user.equals(this.studyGroup.getMentor())));
    }

    public boolean isOverdue() {
        return LocalDateTime.now().isAfter(endDate);
    }
}