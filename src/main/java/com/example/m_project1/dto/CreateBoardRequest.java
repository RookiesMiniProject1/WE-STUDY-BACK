package com.example.m_project1.dto;

import com.example.m_project1.entity.Board;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


@Getter
@NoArgsConstructor
public class CreateBoardRequest {
    @NotBlank(message = "제목은 필수입니다")
    private String title;

    @NotBlank(message = "내용은 필수입니다")
    private String content;

    @NotNull(message = "게시판 유형은 필수입니다")
    private Board.BoardType boardType;

    private Long groupId;  // 그룹 게시판인 경우에만 사용
}
