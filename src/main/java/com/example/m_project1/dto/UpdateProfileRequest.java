package com.example.m_project1.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이어야 합니다")
    private String email; // 이메일 필드 추가

    private String career; // 멘토일 경우 사용
    private String techStack; // 멘토일 경우 사용
    private String interestSkills; // 관심 기술 추가
}
