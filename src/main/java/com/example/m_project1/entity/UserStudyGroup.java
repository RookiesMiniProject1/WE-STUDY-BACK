package com.example.m_project1.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "user_study_group")
public class UserStudyGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_group_id", nullable = false)
    private StudyGroup studyGroup;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role=Role.MEMBER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudyGroup.JoinStatus status = StudyGroup.JoinStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Column()
    private LocalDateTime approvedAt;

    @Column()
    private String rejectReason;

    // UserStudyGroup.java
    public enum Role {
        LEADER, MEMBER, MENTOR  // MENTOR 추가
    }

    @Builder
    public UserStudyGroup(User user, StudyGroup studyGroup, Role role, StudyGroup.JoinStatus status) {
        this.user = Objects.requireNonNull(user, "사용자는 필수입니다.");
        this.studyGroup = Objects.requireNonNull(studyGroup, "스터디 그룹은 필수입니다.");
        this.role = Objects.requireNonNull(role, "역할은 필수입니다.");

        // status가 명시적으로 전달되지 않은 경우 기본값 설정
        this.status = (status != null) ? status : StudyGroup.JoinStatus.PENDING;
        this.joinedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.joinedAt == null) {
            this.joinedAt = LocalDateTime.now(); // 기본값으로 현재 시간 설정
        }
    }

    public void approve() {
        this.status = StudyGroup.JoinStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
    }

    public void reject(String reason) {
        this.status = StudyGroup.JoinStatus.REJECTED;
        this.rejectReason = reason;
    }

    public boolean hasPermission(User user) {
        return this.user.equals(user) &&
                (this.role == Role.LEADER || this.status == StudyGroup.JoinStatus.APPROVED);
    }

    public boolean isActive() {
        return this.status == StudyGroup.JoinStatus.APPROVED;
    }

    public boolean isPending() {
        return this.status == StudyGroup.JoinStatus.PENDING;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserStudyGroup that = (UserStudyGroup) o;
        return Objects.equals(user.getUserId(), that.user.getUserId()) &&
                Objects.equals(studyGroup.getId(), that.studyGroup.getId()) &&
                status == that.status &&
                role == that.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                user != null ? user.getUserId() : null,
                studyGroup != null ? studyGroup.getId() : null,
                status,
                role
        );
    }


}

