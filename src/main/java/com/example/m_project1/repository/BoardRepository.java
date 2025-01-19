package com.example.m_project1.repository;

import com.example.m_project1.dto.BoardDto;
import com.example.m_project1.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
    List<Board> findByStudyGroupIdOrderByPostTimeDesc(Long groupId);

    List<Board> findByBoardTypeOrderByPostTimeDesc(Board.BoardType boardType);

    List<Board> findByStudyGroupIdAndBoardTypeOrderByPostTimeDesc(Long groupId, Board.BoardType boardType);

    Optional<Board> findByIdAndStudyGroupId(Long id, Long groupId);

    boolean existsByIdAndAuthor_UserId(Long id, Long authorId);

    void deleteByStudyGroupId(Long groupId);

    void deleteByAuthor_UserId(Long userId);


}
