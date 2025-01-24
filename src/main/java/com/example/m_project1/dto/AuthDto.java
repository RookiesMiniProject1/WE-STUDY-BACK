package com.example.m_project1.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

public class AuthDto {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class RegisterRequest {
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이어야 합니다")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
                message = "비밀번호는 8자 이상, 영문, 숫자, 특수문자를 포함해야 합니다")
        private String password;

        @NotBlank(message = "사용자 역할은 필수입니다 (MENTOR 또는 MENTEE)")
        private String role;

        private String career;
        private String techStack;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이어야 합니다")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다")
        private String password;
    }

    @Getter
    @NoArgsConstructor
    public static class AuthResponse {
        private String token;
        private String email;
        private String role;
        private String type = "Bearer";
        private LocalDateTime issuedAt;
        private LocalDateTime expiresAt;

        public AuthResponse(String token, String refreshToken, String email, String role) {
            this.token = token;
            this.email = email;
            this.role = role;
            this.type = "Bearer";
            this.issuedAt = LocalDateTime.now();
            this.expiresAt = this.issuedAt.plusMinutes(60);
        }

    }
}