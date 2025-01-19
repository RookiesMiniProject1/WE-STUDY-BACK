package com.example.m_project1.dto;

import com.example.m_project1.entity.StudyGroup;
import com.example.m_project1.entity.UserStudyGroup;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class JoinRequestDto {
    private Long userId;
    private String userEmail;
    private StudyGroup.JoinStatus status;
    private LocalDateTime requestedAt;

    public JoinRequestDto(UserStudyGroup joinRequest) {
        this.userId = joinRequest.getUser().getUserId();
        this.userEmail = joinRequest.getUser().getEmail();
        this.status = joinRequest.getStatus();
        this.requestedAt = joinRequest.getJoinedAt();
    }
}
