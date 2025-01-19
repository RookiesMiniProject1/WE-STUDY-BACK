package com.example.m_project1.dto;

import com.example.m_project1.entity.KanbanBoard;
import com.example.m_project1.entity.KanbanItem;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class KanbanBoardStatusResponse {
    private List<KanbanItemResponse> todoItems;
    private List<KanbanItemResponse> inProgressItems;
    private List<KanbanItemResponse> doneItems;

    public KanbanBoardStatusResponse(KanbanBoard board) {
        this.todoItems = board.getItemsByStatus(KanbanItem.Status.TODO)
                .stream().map(KanbanItemResponse::new).collect(Collectors.toList());
        this.inProgressItems = board.getItemsByStatus(KanbanItem.Status.IN_PROGRESS)
                .stream().map(KanbanItemResponse::new).collect(Collectors.toList());
        this.doneItems = board.getItemsByStatus(KanbanItem.Status.DONE)
                .stream().map(KanbanItemResponse::new).collect(Collectors.toList());
    }
}