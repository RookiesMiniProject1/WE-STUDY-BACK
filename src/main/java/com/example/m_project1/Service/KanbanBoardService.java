package com.example.m_project1.Service;

import com.example.m_project1.dto.CreateKanbanBoardRequest;
import com.example.m_project1.dto.KanbanBoardStatusResponse;
import com.example.m_project1.entity.KanbanBoard;
import com.example.m_project1.entity.StudyGroup;
import com.example.m_project1.entity.User;
import com.example.m_project1.exception.AccessDeniedException;
import com.example.m_project1.exception.InvalidOperationException;
import com.example.m_project1.exception.ResourceNotFoundException;
import com.example.m_project1.repository.KanbanBoardRepository;
import com.example.m_project1.repository.StudyGroupRepository;
import com.example.m_project1.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KanbanBoardService {
    private final KanbanBoardRepository kanbanBoardRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final UserRepository userRepository;

    @Transactional
    public KanbanBoard createBoard(Long groupId, Long userId, CreateKanbanBoardRequest request) {
        StudyGroup group = findStudyGroupById(groupId);
        User user = findUserById(userId);

        // 그룹당 하나의 칸반보드만 허용
        if (kanbanBoardRepository.findByStudyGroupId(groupId).isPresent()) {
            throw new InvalidOperationException("이미 칸반 보드가 존재합니다.");
        }

        // 권한 체크 (멘토나 리더만 생성 가능)
        if (!group.isLeader(user) && !user.equals(group.getMentor())) {
            throw new AccessDeniedException("칸반 보드를 생성할 권한이 없습니다.");
        }

        KanbanBoard board = KanbanBoard.builder()
                .studyGroup(group)
                .title(request.getTitle())
                .description(request.getDescription())
                .build();

        return kanbanBoardRepository.save(board);
    }

    @Transactional
    public KanbanBoard updateBoard(Long boardId, Long userId, CreateKanbanBoardRequest request) {
        KanbanBoard board = findBoardById(boardId);
        User user = findUserById(userId);

        if (!board.canManage(user)) {
            throw new AccessDeniedException("칸반 보드를 수정할 권한이 없습니다.");
        }

        board.update(request.getTitle(), request.getDescription());
        return kanbanBoardRepository.save(board);
    }

    @Transactional
    public void deleteBoard(Long boardId, Long userId) {
        KanbanBoard board = findBoardById(boardId);
        User user = findUserById(userId);

        if (!board.canManage(user)) {
            throw new AccessDeniedException("칸반 보드를 삭제할 권한이 없습니다.");
        }
        // 삭제로직 변경
        // 명시적으로 아이템 삭제
        if (!board.getItems().isEmpty()) {
            board.getItems().clear(); // 연관된 아이템 명시적 제거
        }

        // 스터디 그룹과의 연관관계 제거
        if (board.getStudyGroup() != null) {
            board.getStudyGroup().setKanbanBoard(null);
        }

        // 삭제
        kanbanBoardRepository.delete(board);
        kanbanBoardRepository.flush(); // 즉시 DB에 반영
    }

    public KanbanBoard getBoard(Long groupId, Long userId) {
        StudyGroup group = findStudyGroupById(groupId);
        User user = findUserById(userId);

        if (!group.isMember(user)) {
            throw new AccessDeniedException("해당 그룹의 멤버만 칸반 보드를 볼 수 있습니다.");
        }

        return kanbanBoardRepository.findByStudyGroupId(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("칸반 보드를 찾을 수 없습니다."));
    }

    public KanbanBoardStatusResponse getBoardStatus(Long groupId, Long userId) {
        KanbanBoard board = getBoard(groupId, userId);
        return new KanbanBoardStatusResponse(board);
    }

    private KanbanBoard findBoardById(Long boardId) {
        return kanbanBoardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("칸반 보드를 찾을 수 없습니다."));
    }

    private StudyGroup findStudyGroupById(Long groupId) {
        return studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("스터디 그룹을 찾을 수 없습니다."));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
    }
}