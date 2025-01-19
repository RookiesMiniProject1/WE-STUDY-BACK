package com.example.m_project1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
//이건지울까?
@Getter
@NoArgsConstructor
public class UpdateStudyGroupRequest {
    @NotBlank(message = "제목은 필수 입력값입니다")
    private String title;

    @NotBlank(message = "설명은 필수 입력값입니다")
    private String description;

    private Integer maxMembers;
    private Boolean isRecruiting;
}