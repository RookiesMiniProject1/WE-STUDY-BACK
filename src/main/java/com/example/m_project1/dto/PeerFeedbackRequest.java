package com.example.m_project1.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PeerFeedbackRequest {
    @NotBlank(message = "피드백 내용은 필수입니다")
    @Size(max = 1000, message = "피드백은 1000자를 초과할 수 없습니다")
    private String feedback;

    @Min(value = 0, message = "점수는 0점 이상이어야 합니다")
    @Max(value = 5, message = "점수는 5점을 초과할 수 없습니다")
    private Integer score;
}
