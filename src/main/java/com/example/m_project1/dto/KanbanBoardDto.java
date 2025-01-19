package com.example.m_project1.dto;

import com.example.m_project1.entity.KanbanBoard;
import com.example.m_project1.entity.KanbanItem;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class KanbanBoardDto {

    @Getter
    @NoArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "제목은 필수입니다")
        private String title;
        private String description;
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateRequest {
        private String title;
        private String description;
    }

    @Getter
    @NoArgsConstructor
    public static class Response {
        private Long id;
        private String title;
        private String description;
        private Long groupId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<KanbanItemDto.Response> items;

        public Response(KanbanBoard board) {
            this.id = board.getId();
            this.title = board.getTitle();
            this.description = board.getDescription();
            this.groupId = board.getStudyGroup().getId();
            this.createdAt = board.getCreatedAt();
            this.updatedAt = board.getUpdatedAt();
            this.items = board.getItems().stream()
                    .map(KanbanItemDto.Response::new)
                    .collect(Collectors.toList());
        }
    }

    @Getter
    @NoArgsConstructor
    public static class BoardStatusResponse {
        private List<KanbanItemDto.Response> todoItems;
        private List<KanbanItemDto.Response> inProgressItems;
        private List<KanbanItemDto.Response> doneItems;

        public BoardStatusResponse(List<KanbanItem> items) {
            this.todoItems = items.stream()
                    .filter(item -> item.getStatus() == KanbanItem.Status.TODO)
                    .map(KanbanItemDto.Response::new)
                    .collect(Collectors.toList());

            this.inProgressItems = items.stream()
                    .filter(item -> item.getStatus() == KanbanItem.Status.IN_PROGRESS)
                    .map(KanbanItemDto.Response::new)
                    .collect(Collectors.toList());

            this.doneItems = items.stream()
                    .filter(item -> item.getStatus() == KanbanItem.Status.DONE)
                    .map(KanbanItemDto.Response::new)
                    .collect(Collectors.toList());
        }
    }
}