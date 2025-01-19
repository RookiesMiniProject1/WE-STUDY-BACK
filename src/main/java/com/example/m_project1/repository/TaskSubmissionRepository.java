package com.example.m_project1.repository;

import com.example.m_project1.entity.TaskSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskSubmissionRepository extends JpaRepository<TaskSubmission, Long> {
    List<TaskSubmission> findByTaskId(Long taskId);
    Optional<TaskSubmission> findByTask_IdAndUser_UserId(Long taskId, Long userId);

    List<TaskSubmission> findByUser_UserId(Long userId);

    boolean existsByTask_IdAndUser_UserId(Long taskId, Long userId);


    void deleteByTaskId(Long taskId);
}
