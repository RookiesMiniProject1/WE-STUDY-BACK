package com.example.m_project1.dto;

import com.example.m_project1.entity.Board;
import com.example.m_project1.entity.Calendar;
import com.example.m_project1.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

public class CommentDto {
    @Getter
    @NoArgsConstructor
    public static class Request {
        @NotBlank(message = "내용은 필수입니다.")
        private String comment;
    }
    @Getter
    @Builder
    public static class Response {

        private Long id;
        private String comment;
        private LocalDateTime createdDate;
        private LocalDateTime modifiedDate;
        private Long boardID;
        private Long userID; // 작성자
    }

}
