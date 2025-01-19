package com.example.m_project1.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Valid
public class CreateStudyGroupRequest {
    @NotBlank(message = "제목은 필수 입력값입니다")
    @Size(min = 2, max = 100, message = "제목은 2자 이상 100자 이하여야 합니다")
    private String title;

    @NotBlank(message = "설명은 필수 입력값입니다")
    @Size(max = 1000, message = "설명은 1000자 이하여야 합니다")
    private String description;

    @Min(value = 2, message = "최소 인원은 2명 이상이어야 합니다")
    @Max(value = 20, message = "최대 인원은 20명을 초과할 수 없습니다")
    private int maxMembers;
}