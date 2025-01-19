package com.example.m_project1.controller;

import com.example.m_project1.Service.BoardService;
import com.example.m_project1.dto.*;
import com.example.m_project1.entity.Board;
import com.example.m_project1.entity.KanbanBoard;
import com.example.m_project1.exception.InvalidOperationException;
import com.example.m_project1.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.example.m_project1.entity.User;
import com.example.m_project1.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> createPost(
            @Valid @RequestBody CreateBoardRequest request,
            Authentication authentication) {
        try {
            Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
            Board board = boardService.createPost(userId, request);
            return ResponseEntity.ok(new BoardResponse(board));
        } catch (Exception e) {
            log.error("게시글 작성 중 오류 발생: ", e);
            throw new InvalidOperationException(e.getMessage());
        }
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPost(
            @PathVariable Long postId,
            Authentication authentication) {
        Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        Board board = boardService.getPost(postId, userId);
        return ResponseEntity.ok(new BoardResponse(board));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<?> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody UpdateBoardRequest request,
            Authentication authentication) {
        Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        Board board = boardService.updatePost(postId, userId, request);
        return ResponseEntity.ok(new BoardResponse(board));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(
            @PathVariable Long postId,
            Authentication authentication) {
        Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        boardService.deletePost(postId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<?> getGroupPosts(
            @PathVariable Long groupId,
            @RequestParam(required = false) Board.BoardType type,
            Authentication authentication) {
        Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        List<Board> boards = boardService.getGroupPosts(groupId, userId, type);
        List<BoardResponse> response = boards.stream()
                .map(BoardResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<?> getPosts(
            @RequestParam(required = false) Board.BoardType type,
            Authentication authentication) {
        Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        List<Board> boards = boardService.getPosts(type);
        List<BoardResponse> response = boards.stream()
                .map(BoardResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
