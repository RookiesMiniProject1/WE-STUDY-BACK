package com.example.m_project1.Service;

import com.example.m_project1.dto.CreateCommentRequest;
import com.example.m_project1.dto.UpdateCommentRequest;
import com.example.m_project1.entity.Board;
import com.example.m_project1.entity.Comment;
import com.example.m_project1.entity.User;
import com.example.m_project1.exception.AccessDeniedException;
import com.example.m_project1.exception.ResourceNotFoundException;
import com.example.m_project1.repository.BoardRepository;
import com.example.m_project1.repository.CommentRepository;
import com.example.m_project1.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    @Transactional
    public Comment createComment(Long boardId, Long userId, CreateCommentRequest request) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다."));

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        // 그룹 게시글인 경우 멤버 확인
        if (board.getStudyGroup() != null && !board.getStudyGroup().isMember(author)) {
            throw new AccessDeniedException("해당 그룹의 멤버만 댓글을 작성할 수 있습니다.");
        }

        Comment comment = Comment.builder()
                .content(request.getContent())
                .board(board)
                .author(author)
                .build();

        return commentRepository.save(comment);
    }

    @Transactional
    public Comment updateComment(Long commentId, Long userId, UpdateCommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("댓글을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        if (!comment.canModify(user)) {
            throw new AccessDeniedException("댓글을 수정할 권한이 없습니다.");
        }

        comment.update(request.getContent());
        return commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("댓글을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        if (!comment.canModify(user)) {
            throw new AccessDeniedException("댓글을 삭제할 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }

    public List<Comment> getComments(Long boardId, Long userId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        // 그룹 게시글인 경우 멤버 확인
        if (board.getStudyGroup() != null && !board.getStudyGroup().isMember(user)) {
            throw new AccessDeniedException("해당 그룹의 멤버만 댓글을 볼 수 있습니다.");
        }

        return commentRepository.findByBoardIdOrderByCreatedAtDesc(boardId);
    }
}