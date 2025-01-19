package com.example.m_project1.Service;

import com.example.m_project1.dto.CreateKanbanItemRequest;
import com.example.m_project1.dto.UpdateKanbanItemRequest;
import com.example.m_project1.entity.*;
import com.example.m_project1.exception.AccessDeniedException;
import com.example.m_project1.exception.InvalidOperationException;
import com.example.m_project1.exception.ResourceNotFoundException;
import com.example.m_project1.repository.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KanbanItemService {
    private final KanbanItemRepository kanbanItemRepository;
    private final KanbanBoardRepository kanbanBoardRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    @Transactional
    public KanbanItem createItem(Long boardId, Long userId, CreateKanbanItemRequest request) {
        KanbanBoard board = findBoardById(boardId);
        User creator = findUserById(userId);
        StudyGroup group = board.getStudyGroup();

        if (!group.isMember(creator)) {
            throw new AccessDeniedException("그룹 멤버만 아이템을 생성할 수 있습니다.");
        }

        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = findUserById(request.getAssigneeId());
            if (!group.isMember(assignee)) {
                throw new InvalidOperationException("그룹 멤버에게만 할당할 수 있습니다.");
            }
        }

        Task relatedTask = null;
        if (request.getTaskId() != null) {
            relatedTask = findTaskById(request.getTaskId());
            if (!relatedTask.getStudyGroup().equals(group)) {
                throw new InvalidOperationException("해당 그룹의 과제만 연결할 수 있습니다.");
            }
        }

        KanbanItem item = KanbanItem.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .kanbanBoard(board)
                .studyGroup(group)
                .assignee(assignee)
                .priority(request.getPriority())
                .relatedTask(relatedTask)
                .build();

        board.addItem(item);
        return kanbanItemRepository.save(item);
    }

    @Transactional
    public KanbanItem updateItem(Long itemId, Long userId, UpdateKanbanItemRequest request) {
        KanbanItem item = findItemById(itemId);
        User user = findUserById(userId);

        if (!item.canModify(user)) {
            throw new AccessDeniedException("아이템을 수정할 권한이 없습니다.");
        }

        User newAssignee = null;
        if (request.getAssigneeId() != null) {
            newAssignee = findUserById(request.getAssigneeId());
            if (!item.getStudyGroup().isMember(newAssignee)) {
                throw new InvalidOperationException("그룹 멤버에게만 할당할 수 있습니다.");
            }
        }

        item.update(
                request.getTitle(),
                request.getDescription(),
                newAssignee,
                request.getPriority(),
                request.getStatus()
        );

        return kanbanItemRepository.save(item);
    }

    @Transactional
    public void deleteItem(Long itemId, Long userId) {
        KanbanItem item = findItemById(itemId);
        User user = findUserById(userId);

        if (!item.canModify(user)) {
            throw new AccessDeniedException("아이템을 삭제할 권한이 없습니다.");
        }

        item.getKanbanBoard().removeItem(item);
        kanbanItemRepository.delete(item);
    }

    @Transactional
    public void syncWithTask(Long taskId, Task.Status taskStatus) {
        List<KanbanItem> items = kanbanItemRepository.findByRelatedTaskId(taskId);
        KanbanItem.Status newStatus = switch (taskStatus) {
            case IN_PROGRESS -> KanbanItem.Status.IN_PROGRESS;
            case COMPLETED -> KanbanItem.Status.DONE;
            default -> KanbanItem.Status.TODO;
        };

        items.forEach(item -> item.updateStatus(newStatus));
        kanbanItemRepository.saveAll(items);
    }

    @Transactional
    public void updateSubmissionStatus(Long taskId, Long userId) {
        List<KanbanItem> items = kanbanItemRepository.findByRelatedTask_IdAndAssignee_UserId(taskId, userId);
        items.forEach(item -> {
            if (item.getStatus() != KanbanItem.Status.DONE) {
                item.updateStatus(KanbanItem.Status.IN_PROGRESS);
            }
        });
        kanbanItemRepository.saveAll(items);
    }

    public List<KanbanItem> getGroupItems(Long groupId, Long userId) {
        StudyGroup group = findGroupById(groupId);
        User user = findUserById(userId);

        if (!group.isMember(user)) {
            throw new AccessDeniedException("그룹 멤버만 아이템을 조회할 수 있습니다.");
        }

        return kanbanItemRepository.findByStudyGroupId(groupId);
    }

    private KanbanBoard findBoardById(Long boardId) {
        return kanbanBoardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("칸반 보드를 찾을 수 없습니다."));
    }

    private KanbanItem findItemById(Long itemId) {
        return kanbanItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("칸반 아이템을 찾을 수 없습니다."));
    }

    private StudyGroup findGroupById(Long groupId) {
        return studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("스터디 그룹을 찾을 수 없습니다."));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
    }

    private Task findTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("과제를 찾을 수 없습니다."));
    }

    @Transactional
    public void deleteByTaskId(Long taskId) {
        List<KanbanItem> items = kanbanItemRepository.findByRelatedTaskId(taskId);

        if (items.isEmpty()) {
            log.info("No KanbanItems found for Task ID: {}", taskId);
            return;
        }

        // 각 아이템의 보드에서 제거
        items.forEach(item -> {
            KanbanBoard board = item.getKanbanBoard();
            if (board != null) {
                board.removeItem(item);
            }
        });

        // 연관된 아이템 삭제
        kanbanItemRepository.deleteAll(items);
        log.info("Deleted {} KanbanItems related to Task ID: {}", items.size(), taskId);
    }

    @Transactional  //유저아이디 쓰고있음, 그룹아이디로 바꾸는거 생각해보자
    public void updateItemStatusToComplete(Long taskId, Long userId) {
        List<KanbanItem> items = kanbanItemRepository.findByRelatedTask_IdAndAssignee_UserId(taskId, userId);
        items.forEach(item -> item.updateStatus(KanbanItem.Status.DONE));
        kanbanItemRepository.saveAll(items);
    }

}