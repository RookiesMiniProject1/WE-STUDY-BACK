package com.example.m_project1.dto;

import com.example.m_project1.entity.KanbanBoard;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class KanbanBoardResponse {
    private Long id;
    private Long groupId;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<KanbanItemResponse> items;

    public KanbanBoardResponse(KanbanBoard board) {
        this.id = board.getId();
        this.groupId = board.getStudyGroup().getId();
        this.title = board.getTitle();
        this.description = board.getDescription();
        this.createdAt = board.getCreatedAt();
        this.updatedAt = board.getUpdatedAt();
        this.items = board.getItems().stream()
                .map(KanbanItemResponse::new)
                .collect(Collectors.toList());
    }
}
