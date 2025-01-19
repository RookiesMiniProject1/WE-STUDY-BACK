package com.example.m_project1.controller;

import com.example.m_project1.Service.KanbanBoardService;
import com.example.m_project1.dto.CreateKanbanBoardRequest;
import com.example.m_project1.dto.KanbanBoardDto;
import com.example.m_project1.dto.KanbanBoardResponse;
import com.example.m_project1.dto.KanbanBoardStatusResponse;
import com.example.m_project1.entity.KanbanBoard;
import com.example.m_project1.entity.User;
import com.example.m_project1.exception.InvalidOperationException;
import com.example.m_project1.repository.UserRepository;
import com.example.m_project1.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/groups/{groupId}/board")
@RequiredArgsConstructor
public class KanbanBoardController {
    private final KanbanBoardService kanbanBoardService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> createBoard(
            @PathVariable Long groupId,
            @Valid @RequestBody CreateKanbanBoardRequest request,
            Authentication authentication) {
        try {
            Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
            KanbanBoard board = kanbanBoardService.createBoard(groupId, userId, request);
            return ResponseEntity.ok(new KanbanBoardResponse(board));
        } catch (Exception e) {
            log.error("칸반 보드 생성 중 오류 발생: ", e);
            throw new InvalidOperationException(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getBoard(
            @PathVariable Long groupId,
            Authentication authentication) {
        Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        KanbanBoard board = kanbanBoardService.getBoard(groupId, userId);
        return ResponseEntity.ok(new KanbanBoardResponse(board));
    }

    @GetMapping("/status")
    public ResponseEntity<?> getBoardStatus(
            @PathVariable Long groupId,
            Authentication authentication) {
        Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        KanbanBoardStatusResponse response = kanbanBoardService.getBoardStatus(groupId, userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{boardId}")
    public ResponseEntity<?> updateBoard(
            @PathVariable Long groupId,
            @PathVariable Long boardId,
            @Valid @RequestBody CreateKanbanBoardRequest request,
            Authentication authentication) {
        Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        KanbanBoard board = kanbanBoardService.updateBoard(boardId, userId, request);
        return ResponseEntity.ok(new KanbanBoardResponse(board));
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<?> deleteBoard(
            @PathVariable Long groupId,
            @PathVariable Long boardId,
            Authentication authentication) {
        Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        kanbanBoardService.deleteBoard(boardId, userId);
        return ResponseEntity.ok().build();
    }
}