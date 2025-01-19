package com.example.m_project1.dto;

import com.example.m_project1.entity.UserStudyGroup;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
//필요한가?
@Getter
@NoArgsConstructor
public class UserStudyGroupResponseDto {
    private Long userId;
    private String email;
    private UserStudyGroup.Role role;
    private LocalDateTime joinedAt;

    public UserStudyGroupResponseDto(UserStudyGroup userStudyGroup) {
        this.userId = userStudyGroup.getUser().getUserId();
        this.email = userStudyGroup.getUser().getEmail();
        this.role = userStudyGroup.getRole();
        this.joinedAt = userStudyGroup.getJoinedAt();
    }
}
