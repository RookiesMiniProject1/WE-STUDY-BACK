package com.example.m_project1.dto;

import com.example.m_project1.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long userId;
    private String email;
    private User.Role role;
    private String career;
    private String techStack;
}
