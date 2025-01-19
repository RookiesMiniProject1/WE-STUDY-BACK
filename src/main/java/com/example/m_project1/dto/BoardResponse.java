package com.example.m_project1.dto;

import com.example.m_project1.entity.Board;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class BoardResponse {
    private Long id;
    private String title;
    private String content;
    private String authorEmail;
    private Long authorId;
    private Board.BoardType boardType;
    private Long groupId;
    private String groupTitle;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CommentResponse> comments;

    public BoardResponse(Board board) {
        this.id = board.getId();
        this.title = board.getTitle();
        this.content = board.getContent();
        this.authorEmail = board.getAuthor().getEmail();
        this.authorId = board.getAuthor().getUserId();
        this.boardType = board.getBoardType();

        if (board.getStudyGroup() != null) {
            this.groupId = board.getStudyGroup().getId();
            this.groupTitle = board.getStudyGroup().getTitle();
        }

        this.createdAt = board.getPostTime();
        this.updatedAt = board.getEditTime();

        this.comments = board.getComments().stream()
                .map(CommentResponse::new)
                .collect(Collectors.toList());
    }
}
