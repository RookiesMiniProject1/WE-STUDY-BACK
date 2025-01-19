package com.example.m_project1.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.Objects;


@Entity
@Getter
@NoArgsConstructor
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Builder
    public Comment(String content, Board board, User author) {
        this.content = Objects.requireNonNull(content, "내용은 필수입니다.");
        this.board = Objects.requireNonNull(board, "게시글은 필수입니다.");
        this.author = Objects.requireNonNull(author, "작성자는 필수입니다.");
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public void update(String content) {
        this.content = Objects.requireNonNull(content, "내용은 필수입니다.");
        this.updatedAt = LocalDateTime.now();
    }

    public boolean canModify(User user) {
        return this.author.equals(user) ||
                (this.board.getStudyGroup() != null &&
                        (this.board.getStudyGroup().isLeader(user) ||
                                user.equals(this.board.getStudyGroup().getMentor())));
    }
}