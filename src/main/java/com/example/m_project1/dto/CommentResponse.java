package com.example.m_project1.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import com.example.m_project1.entity.Comment;

@Getter
@NoArgsConstructor
public class CommentResponse {
    private Long id;
    private String content;
    private Long authorId;
    private String authorEmail;
    private Long boardId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CommentResponse(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.authorId = comment.getAuthor().getUserId();
        this.authorEmail = comment.getAuthor().getEmail();
        this.boardId = comment.getBoard().getId();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
    }
}
