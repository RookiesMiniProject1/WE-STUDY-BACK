package com.example.m_project1.dto;

import com.example.m_project1.entity.Board;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class BoardDto {
    @Getter
    @NoArgsConstructor
    public static class Request {
        @NotBlank(message = "제목, 내용은 필수입니다")
        private String title;
        private String content;
        private String author;
        private int groupID;

        @Enumerated(EnumType.STRING)
        private Board.BoardType boardType;

    }
    @Getter
    @NoArgsConstructor
    public static class Response {
        private Long id;
        private String title;
        private String content;
        private String author;
        private LocalDateTime postTime;
        private LocalDateTime editTime;
        private int groupID;
        private Board.BoardType boardType;
        public Response(Board board)
        {
            this.id = board.getId();
            this.title = board.getTitle();
            this.content = board.getContents().toString();
            this.author = String.valueOf(board.getAuthor());
            this.postTime = board.getPostTime();
            this.editTime = board.getEditTime();
            this.groupID = Math.toIntExact(board.getStudyGroup() != null ? board.getStudyGroup().getId() : null); // 수정
            this.boardType = board.getBoardType();
        }
    }

}
