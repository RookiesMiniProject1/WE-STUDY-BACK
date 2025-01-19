package com.example.m_project1.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

public class CommonDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiResponse<T> {
        private boolean success; // 성공 여부
        private String message; // 응답 메시지
        private T data; // 데이터
        private HttpStatus status; // HTTP 상태 코드

        // 성공 응답
        public static <T> ApiResponse<T> success(String message, T data) {
            return new ApiResponse<>(true, message, data, HttpStatus.OK);
        }

        // 에러 응답
        public static <T> ApiResponse<T> error(String message, HttpStatus status) {
            return new ApiResponse<>(false, message, null, status);
        }
    }
}
