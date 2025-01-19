package com.example.m_project1.dto;
//스터디그룹 초대
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InviteGroupRequest {
    private Long invitedUserId;  // 초대할 사용자 ID
}