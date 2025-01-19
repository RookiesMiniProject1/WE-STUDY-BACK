package com.example.m_project1.Service;

import com.example.m_project1.dto.CreateBoardRequest;
import com.example.m_project1.dto.UpdateBoardRequest;
import com.example.m_project1.entity.*;
import com.example.m_project1.exception.AccessDeniedException;
import com.example.m_project1.exception.InvalidOperationException;
import com.example.m_project1.exception.ResourceNotFoundException;
import com.example.m_project1.repository.BoardRepository;
import com.example.m_project1.repository.StudyGroupRepository;
import com.example.m_project1.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {
    private final BoardRepository boardRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final UserRepository userRepository;

    @Transactional
    public Board createPost(Long userId, CreateBoardRequest request) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        StudyGroup studyGroup = null;
        if (request.getGroupId() != null) {
            studyGroup = studyGroupRepository.findById(request.getGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("스터디 그룹을 찾을 수 없습니다."));

            if (!studyGroup.isMember(author)) {
                throw new AccessDeniedException("해당 그룹의 멤버만 글을 작성할 수 있습니다.");
            }
        }

        // 권한 검증
        validateWritePermission(author, studyGroup, request.getBoardType());

        Board board = Board.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .author(author)
                .studyGroup(studyGroup)
                .boardType(request.getBoardType())
                .build();

        return boardRepository.save(board);
    }

    public Board getPost(Long postId, Long userId) {
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        if (!board.canAccess(user)) {
            throw new AccessDeniedException("해당 게시글에 접근할 수 없습니다.");
        }

        return board;
    }

    @Transactional
    public Board updatePost(Long postId, Long userId, UpdateBoardRequest request) {
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        if (!board.canModify(user)) {
            throw new AccessDeniedException("게시글을 수정할 권한이 없습니다.");
        }

        board.update(request.getTitle(), request.getContent());
        return boardRepository.save(board);
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        if (!board.canModify(user)) {
            throw new AccessDeniedException("게시글을 삭제할 권한이 없습니다.");
        }

        boardRepository.delete(board);
    }

    public List<Board> getGroupPosts(Long groupId, Long userId, Board.BoardType boardType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("스터디 그룹을 찾을 수 없습니다."));

        if (!group.isMember(user)) {
            throw new AccessDeniedException("해당 그룹의 게시판에 접근할 수 없습니다.");
        }

        if (boardType != null) {
            return boardRepository.findByStudyGroupIdAndBoardTypeOrderByPostTimeDesc(groupId, boardType);
        }
        return boardRepository.findByStudyGroupIdOrderByPostTimeDesc(groupId);
    }

    private void validateWritePermission(User user, StudyGroup group, Board.BoardType boardType) {
        switch (boardType) {
            case NOTICE -> {
                if (!user.isMentor()) {
                    throw new AccessDeniedException("공지사항은 멘토만 작성할 수 있습니다.");
                }
            }
            case GROUP_NOTICE -> {
                if (group == null) {
                    throw new InvalidOperationException("그룹 정보가 필요합니다.");
                }
                if (!group.isLeader(user) && !user.equals(group.getMentor())) {
                    throw new AccessDeniedException("그룹 공지는 그룹 리더와 멘토만 작성할 수 있습니다.");
                }
            }
            case GROUP_DISCUSSION, GROUP_RESOURCE -> {
                if (group == null) {
                    throw new InvalidOperationException("그룹 정보가 필요합니다.");
                }
                if (!group.isMember(user)) {
                    throw new AccessDeniedException("그룹 멤버만 작성할 수 있습니다.");
                }
            }
        }
    }

    public List<Board> getPosts(Board.BoardType boardType) {
        if (boardType != null) {
            return boardRepository.findByBoardTypeOrderByPostTimeDesc(boardType);
        }
        return boardRepository.findAll(); // 모든 게시글 조회
    }




}
