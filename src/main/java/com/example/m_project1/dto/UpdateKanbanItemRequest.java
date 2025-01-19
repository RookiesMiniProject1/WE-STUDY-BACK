package com.example.m_project1.dto;

import com.example.m_project1.entity.KanbanItem;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateKanbanItemRequest {
    private String title;
    private String description;
    private Long assigneeId;
    private Integer priority;
    private KanbanItem.Status status;
}