package com.example.m_project1.repository;

import com.example.m_project1.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByStudyGroupIdOrderByDeadlineAsc(Long groupId);

    List<Task> findByStudyGroupIdAndStatusOrderByDeadlineAsc(Long groupId, Task.Status status);

    boolean existsByStudyGroupIdAndTitle(Long groupId, String title);

}