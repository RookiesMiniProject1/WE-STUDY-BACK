package com.example.m_project1.util;

import com.example.m_project1.entity.StudyGroup;
import com.example.m_project1.entity.User;
import com.example.m_project1.entity.UserStudyGroup;
import com.example.m_project1.exception.AccessDeniedException;
import com.example.m_project1.exception.ResourceNotFoundException;
import com.example.m_project1.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;


@Component
public class SecurityUtils {
    public static final String GROUP_ACCESS_ERROR = "해당 그룹에 접근 권한이 없습니다.";
    public static final String NOT_FOUND_ERROR = "리소스를 찾을 수 없습니다.";
    public static final String PERMISSION_ERROR = "권한이 없습니다.";

    public static Long getUserIdFromAuth(Authentication authentication, UserRepository userRepository) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        return user.getUserId();
    }

    public static void validateGroupMember(StudyGroup group, Long userId) {
        boolean isMember = group.getMembers().stream()
                .anyMatch(member -> member.getUser().getUserId().equals(userId));
        if (!isMember) {
            throw new AccessDeniedException(GROUP_ACCESS_ERROR);
        }
    }

    public static void validateGroupLeader(StudyGroup group, Long userId) {
        boolean isLeader = group.getMembers().stream()
                .anyMatch(member -> member.getUser().getUserId().equals(userId)
                        && member.getRole() == UserStudyGroup.Role.LEADER);
        if (!isLeader) {
            throw new AccessDeniedException(PERMISSION_ERROR);
        }
    }

    public static void validateMentor(StudyGroup group, Long userId) {
        if (group.getMentor() == null || !group.getMentor().getUserId().equals(userId)) {
            throw new AccessDeniedException(PERMISSION_ERROR);
        }
    }
}
