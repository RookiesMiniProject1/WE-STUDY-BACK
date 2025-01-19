package com.example.m_project1.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


@Getter
@NoArgsConstructor
public class CreateKanbanItemRequest {
    @NotBlank(message = "제목은 필수입니다")
    @Size(min = 2, max = 100, message = "제목은 2자 이상 100자 이하여야 합니다")
    private String title;

    @Size(max = 1000, message = "설명은 1000자 이하여야 합니다")
    private String description;

    private Long assigneeId;

    @NotNull(message = "우선순위는 필수입니다")
    @Min(value = 1, message = "우선순위는 1-3 사이여야 합니다")
    @Max(value = 3, message = "우선순위는 1-3 사이여야 합니다")
    private Integer priority;

    private Long taskId;
}