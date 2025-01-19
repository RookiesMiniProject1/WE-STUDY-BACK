package com.example.m_project1.dto;

import com.example.m_project1.entity.Task;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
public class TaskStatusRequest {
    @NotNull(message = "상태는 필수입니다")
    private Task.Status status;
}
