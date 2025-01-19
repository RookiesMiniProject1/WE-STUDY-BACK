package com.example.m_project1.repository;

import com.example.m_project1.entity.KanbanItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KanbanItemRepository extends JpaRepository<KanbanItem, Long> {
    List<KanbanItem> findByStudyGroupId(Long groupId);

    List<KanbanItem> findByKanbanBoardIdOrderByPriorityAsc(Long boardId);

    List<KanbanItem> findByKanbanBoardIdAndStatusOrderByPriorityAsc(Long boardId, KanbanItem.Status status);

    List<KanbanItem> findByAssignee_UserId(Long userId);

    List<KanbanItem> findByRelatedTaskId(Long taskId);

    void deleteByRelatedTaskId(Long taskId);

    List<KanbanItem> findByRelatedTask_IdAndAssignee_UserId(Long taskId, Long userId);

}