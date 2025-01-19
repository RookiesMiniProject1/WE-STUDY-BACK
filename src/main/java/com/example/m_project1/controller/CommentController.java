package com.example.m_project1.controller;

import com.example.m_project1.Service.CommentService;
import com.example.m_project1.dto.CommentResponse;
import com.example.m_project1.dto.CreateCommentRequest;
import com.example.m_project1.dto.UpdateCommentRequest;
import com.example.m_project1.entity.Comment;
import com.example.m_project1.exception.InvalidOperationException;
import com.example.m_project1.repository.UserRepository;
import com.example.m_project1.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request,
            Authentication authentication) {
        try {
            Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
            Comment comment = commentService.createComment(postId, userId, request);
            return ResponseEntity.ok(new CommentResponse(comment));
        } catch (Exception e) {
            log.error("댓글 작성 중 오류 발생: ", e);
            throw new InvalidOperationException(e.getMessage());
        }
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request,
            Authentication authentication) {
        Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        Comment comment = commentService.updateComment(commentId, userId, request);
        return ResponseEntity.ok(new CommentResponse(comment));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            Authentication authentication) {
        Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<?> getComments(
            @PathVariable Long postId,
            Authentication authentication) {
        Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        List<Comment> comments = commentService.getComments(postId, userId);
        List<CommentResponse> response = comments.stream()
                .map(CommentResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
