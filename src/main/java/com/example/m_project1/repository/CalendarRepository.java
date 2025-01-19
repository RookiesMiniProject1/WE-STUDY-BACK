package com.example.m_project1.repository;

import com.example.m_project1.entity.Calendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CalendarRepository extends JpaRepository<Calendar, Long> {
    // 특정 사용자의 일정 조회 (기간별)
    List<Calendar> findByUser_UserIdAndStartDateBetweenOrderByStartDateAsc(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );

    // 특정 그룹의 일정 조회 (기간별)
    List<Calendar> findByStudyGroupIdAndStartDateBetweenOrderByStartDateAsc(
            Long groupId,
            LocalDateTime start,
            LocalDateTime end
    );

    // 특정 과제의 일정 조회
    List<Calendar> findByTaskId(Long taskId);

    // 특정 그룹의 모든 일정 삭제 (그룹 삭제 시 사용)
    void deleteByStudyGroupId(Long groupId);

    // 특정 과제의 모든 일정 삭제 (과제 삭제 시 사용)
    void deleteByTaskId(Long taskId);
}