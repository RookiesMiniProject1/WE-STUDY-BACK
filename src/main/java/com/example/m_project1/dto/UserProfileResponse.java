package com.example.m_project1.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserProfileResponse {

    private Long userId; // 고유 ID
    private String email; // 이메일
    private String role; // 역할
    private String career; // 멘토일 경우 사용
    private String techStack; // 멘토일 경우 사용

    // 멘토를 위한 정적 팩토리 메서드
    public static UserProfileResponse forMentor(Long userId, String email, String role, String career, String techStack) {
        return new UserProfileResponse(userId, email, role, career, techStack);
    }

    // 멘티를 위한 정적 팩토리 메서드
    public static UserProfileResponse forMentee(Long userId, String email, String role) {
        return new UserProfileResponse(userId, email, role, null, null);
    }
}

