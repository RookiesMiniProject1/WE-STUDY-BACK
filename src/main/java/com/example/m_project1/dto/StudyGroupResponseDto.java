package com.example.m_project1.dto;


import com.example.m_project1.entity.StudyGroup;
import com.example.m_project1.entity.User;
import com.example.m_project1.entity.UserStudyGroup;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class StudyGroupResponseDto {
    private Long id;
    private String title;
    private String description;
    private Long mentorId;
    private String mentorEmail;
    private int maxMembers;
    private int currentMember;
    private StudyGroup.Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isRecruiting;
    private StudyGroup.JoinStatus myStatus; // 내 가입 상태
    private List<UserStudyGroupResponseDto> members; // 승인된 상태일 때만 포함
    private List<JoinRequestDto> pendingRequests; // 승인된 상태이면서 리더/멘토일 때만 포함

    @Builder
    public StudyGroupResponseDto(StudyGroup studyGroup, User currentUser) {
        this.id = studyGroup.getId();
        this.title = studyGroup.getTitle();
        this.description = studyGroup.getDescription();
        if (studyGroup.getMentor() != null) {
            this.mentorId = studyGroup.getMentor().getUserId();
            this.mentorEmail = studyGroup.getMentor().getEmail();
        }
        this.maxMembers = studyGroup.getMaxMembers();
        this.currentMember = studyGroup.getCurrentMember();
        this.status = studyGroup.getStatus();
        this.createdAt = studyGroup.getCreatedAt();
        this.updatedAt = studyGroup.getUpdatedAt();
        this.isRecruiting = studyGroup.isRecruiting();

        // 현재 사용자의 가입 상태 확인
        Optional<UserStudyGroup> userMembership = studyGroup.getMembers().stream()
                .filter(member -> member.getUser().equals(currentUser))
                .findFirst();

        Optional<UserStudyGroup> userRequest = studyGroup.getJoinRequests().stream()
                .filter(request -> request.getUser().equals(currentUser))
                .findFirst();

        // 가입 상태 설정
        if (userMembership.isPresent()) {
            // 멘토 승인 상태를 일반 승인 상태로 간주
            StudyGroup.JoinStatus status = userMembership.get().getStatus();
            if (status == StudyGroup.JoinStatus.MENTOR_APPROVED) {
                this.myStatus = StudyGroup.JoinStatus.APPROVED; // 상태 통합
            } else {
                this.myStatus = status;
            }

            // 승인된 멤버는 모든 정보를 볼 수 있음
            this.members = studyGroup.getMembers().stream()
                    .filter(member -> member.getStatus() == StudyGroup.JoinStatus.APPROVED)
                    .map(UserStudyGroupResponseDto::new)
                    .collect(Collectors.toList());

            // 리더나 멘토만 대기 중인 요청을 볼 수 있음
            if (studyGroup.isLeader(currentUser) || currentUser.equals(studyGroup.getMentor())) {
                this.pendingRequests = studyGroup.getJoinRequests().stream()
                        .filter(request -> request.getStatus() == StudyGroup.JoinStatus.PENDING)
                        .map(JoinRequestDto::new)
                        .collect(Collectors.toList());
            }
        } else if (userRequest.isPresent()) {
            // 승인되지 않은 상태면 기본 정보만 표시
            this.myStatus = userRequest.get().getStatus();
            this.members = null;
            this.pendingRequests = null;
        } else {
            // 아무 관계 없는 경우
            this.myStatus = null;
            this.members = null;
            this.pendingRequests = null;
        }
    }

}