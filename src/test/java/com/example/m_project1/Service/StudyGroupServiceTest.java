package com.example.m_project1.Service;

import com.example.m_project1.entity.StudyGroup;
import com.example.m_project1.entity.User;
import com.example.m_project1.repository.StudyGroupRepository;
import com.example.m_project1.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudyGroupServiceTest {
    @Mock
    private StudyGroupRepository studyGroupRepository;

    @InjectMocks
    private StudyGroupService studyGroupService;

    @Test
    @DisplayName("사용자의 스터디 그룹 목록 조회 성공")
    void testGetUserGroups() {
        // Given
        Long userId = 1L;
        List<StudyGroup> expectedGroups = Arrays.asList(
                StudyGroup.builder()
                        .title("Test Group 1")
                        .description("Test Description 1")
                        .maxMembers(5)
                        .build(),
                StudyGroup.builder()
                        .title("Test Group 2")
                        .description("Test Description 2")
                        .maxMembers(5)
                        .build()
        );

        when(studyGroupRepository.findByUserId(userId)).thenReturn(expectedGroups);

        // When
        List<StudyGroup> actualGroups = studyGroupService.getUserGroups(userId);

        // Then
        assertThat(actualGroups).isNotNull();
        assertThat(actualGroups).hasSize(2);
        verify(studyGroupRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("유저 ID가 null인 경우 IllegalArgumentException 발생")
    void testGetUserGroups_NullUserId() {
        // When & Then
        assertThatThrownBy(() -> studyGroupService.getUserGroups(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User ID must not be null");
    }
}