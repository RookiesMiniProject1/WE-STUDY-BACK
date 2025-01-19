package com.example.m_project1.controller;


import com.example.m_project1.Service.KanbanItemService;
import com.example.m_project1.dto.CreateKanbanItemRequest;
import com.example.m_project1.dto.KanbanItemDto;
import com.example.m_project1.dto.KanbanItemResponse;
import com.example.m_project1.dto.UpdateKanbanItemRequest;
import com.example.m_project1.entity.KanbanItem;
import com.example.m_project1.entity.StudyGroup;
import com.example.m_project1.entity.User;
import com.example.m_project1.exception.InvalidOperationException;
import com.example.m_project1.repository.StudyGroupRepository;
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
@RequestMapping("/api/groups/{groupId}/board/{boardId}/items")
@RequiredArgsConstructor
public class KanbanItemController {
    private final KanbanItemService kanbanItemService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> createItem(
            @PathVariable Long groupId,
            @PathVariable Long boardId,
            @Valid @RequestBody CreateKanbanItemRequest request,
            Authentication authentication) {
        try {
            Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
            KanbanItem item = kanbanItemService.createItem(boardId, userId, request);
            return ResponseEntity.ok(new KanbanItemResponse(item));
        } catch (Exception e) {
            log.error("칸반 아이템 생성 중 오류 발생: ", e);
            throw new InvalidOperationException(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getItems(
            @PathVariable Long groupId,
            @PathVariable Long boardId,
            Authentication authentication) {
        Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        List<KanbanItem> items = kanbanItemService.getGroupItems(groupId, userId);
        List<KanbanItemResponse> response = items.stream()
                .map(KanbanItemResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<?> updateItem(
            @PathVariable Long groupId,
            @PathVariable Long boardId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateKanbanItemRequest request,
            Authentication authentication) {
        Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        KanbanItem item = kanbanItemService.updateItem(itemId, userId, request);
        return ResponseEntity.ok(new KanbanItemResponse(item));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> deleteItem(
            @PathVariable Long groupId,
            @PathVariable Long boardId,
            @PathVariable Long itemId,
            Authentication authentication) {
        Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        kanbanItemService.deleteItem(itemId, userId);
        return ResponseEntity.ok().build();
    }
}