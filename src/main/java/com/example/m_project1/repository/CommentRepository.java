package com.example.m_project1.repository;

import com.example.m_project1.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByBoardIdOrderByCreatedAtDesc(Long boardId);
    void deleteByBoardId(Long boardId);
    void deleteByAuthor_UserId(Long userId);

}
