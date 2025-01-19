package com.example.m_project1.Service;

import com.example.m_project1.entity.*;
import com.example.m_project1.exception.AccessDeniedException;
import com.example.m_project1.exception.InvalidOperationException;
import com.example.m_project1.exception.ResourceNotFoundException;
import com.example.m_project1.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class StudyGroupService {
    private final StudyGroupRepository studyGroupRepository;
    private final UserRepository userRepository;
    private final CalendarRepository calendarRepository;
    private final KanbanBoardRepository kanbanBoardRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final TaskRepository taskRepository;
    private final TaskSubmissionRepository taskSubmissionRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public StudyGroup createStudyGroup(String title, String description, Long creatorId, int maxMembers) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        StudyGroup studyGroup = StudyGroup.builder()
                .title(title)
                .description(description)
                .maxMembers(maxMembers)
                //.currentMember(1)  // 생성자 포함
                .isRecruiting(true)
                .mentor(creator.isMentor() ? creator : null)
                .build();

        // 생성자는 바로 승인된 리더로 추가
        studyGroup.addInitialMember(creator, UserStudyGroup.Role.LEADER);

        return studyGroupRepository.save(studyGroup);
    }

    @Transactional
    public StudyGroup updateStudyGroup(Long groupId, String title, String description,
                                       Long userId, Integer maxMembers, Boolean isRecruiting) {
        StudyGroup studyGroup = findGroupById(groupId);
        User user = findUserById(userId);

        if (!studyGroup.isLeader(user) && !user.equals(studyGroup.getMentor())) {
            throw new AccessDeniedException("그룹을 수정할 권한이 없습니다.");
        }

        if (title != null) {
            studyGroup.setTitle(title);
        }
        if (description != null) {
            studyGroup.setDescription(description);
        }
        if (maxMembers != null) {
            if (maxMembers < studyGroup.getCurrentMember()) {
                throw new InvalidOperationException("최대 인원은 현재 인원보다 적을 수 없습니다.");
            }
            studyGroup.setMaxMembers(maxMembers);
        }
        if (isRecruiting != null) {
            studyGroup.setRecruiting(isRecruiting);

            // 모집 마감 시 대기 중인 요청 처리
            if (!isRecruiting) {
                List<UserStudyGroup> pendingRequests = studyGroup.getJoinRequests().stream()
                        .filter(req -> req.getStatus() == StudyGroup.JoinStatus.PENDING)
                        .toList();

                for (UserStudyGroup request : pendingRequests) {
                    request.setStatus(StudyGroup.JoinStatus.REJECTED);
                }
            }
        }

        return studyGroupRepository.save(studyGroup);
    }

    @Transactional
    public void deleteStudyGroup(Long groupId, Long userId) {
        StudyGroup group = findGroupById(groupId);
        User user = findUserById(userId);

        if (!group.isLeader(user) && !user.equals(group.getMentor())) {
            throw new AccessDeniedException("그룹을 삭제할 권한이 없습니다.");
        }

        // 모든 가입 요청 및 멤버십 명시적 제거
        group.getJoinRequests().clear();
        group.getMembers().clear();

        // 현재 멤버 수 0으로 재설정
        group.setCurrentMember(0);


        // 채팅방 및 메시지 삭제
        List<ChatRoom> chatRooms = chatRoomRepository.findByStudyGroupId(groupId);
        for (ChatRoom chatRoom : chatRooms) {
            chatMessageRepository.deleteAll(
                    chatMessageRepository.findByChatRoomIdOrderByTimestampAsc(chatRoom.getId())
            );
        }
        chatRoomRepository.deleteAll(chatRooms);

        // 과제 및 제출물 삭제
        List<Task> tasks = taskRepository.findByStudyGroupIdOrderByDeadlineAsc(groupId);
        for (Task task : tasks) {
            taskSubmissionRepository.deleteAll(
                    taskSubmissionRepository.findByTaskId(task.getId())
            );
        }
        taskRepository.deleteAll(tasks);

        // 게시글 및 댓글 삭제
        List<Board> boards = boardRepository.findByStudyGroupIdOrderByPostTimeDesc(groupId);
        for (Board board : boards) {
            commentRepository.deleteByBoardId(board.getId());
        }
        boardRepository.deleteByStudyGroupId(groupId);

        // 칸반보드 및 일정 삭제
        calendarRepository.deleteByStudyGroupId(groupId);
        kanbanBoardRepository.deleteByStudyGroupId(groupId);

        studyGroupRepository.delete(group);
    }

    @Transactional(readOnly = true)
    public StudyGroup getStudyGroup(Long groupId) {
        return findGroupById(groupId);
    }

    @Transactional(readOnly = true)
    public List<StudyGroup> getAllStudyGroups() {
        return studyGroupRepository.findAll();
    }

    @Transactional
    public StudyGroup requestToJoin(Long groupId, Long userId) {
        StudyGroup group = findGroupById(groupId);
        User user = findUserById(userId);

        if (!group.isRecruiting()) {
            throw new InvalidOperationException("모집이 마감된 그룹입니다.");
        }

        if (group.isFull()) {
            throw new InvalidOperationException("그룹이 가득 찼습니다.");
        }

        if (group.isMember(user)) {
            throw new InvalidOperationException("이미 그룹의 멤버입니다.");
        }

        // 이미 대기 중인 요청이 있는지 확인
        boolean hasPendingRequest = group.getJoinRequests().stream()
                .anyMatch(req -> req.getUser().equals(user) &&
                        req.getStatus() == StudyGroup.JoinStatus.PENDING);

        if (hasPendingRequest) {
            throw new InvalidOperationException("이미 가입 요청이 진행 중입니다.");
        }

        boolean wasRejected = group.getJoinRequests().stream()
                .anyMatch(req -> req.getUser().equals(user) &&
                        req.getStatus() == StudyGroup.JoinStatus.REJECTED);

        if (wasRejected) {
            throw new InvalidOperationException("이전에 거절된 요청이 있습니다. 관리자에게 문의하세요.");
        }

        // PENDING 상태로 가입 요청 추가
        group.addMember(user, UserStudyGroup.Role.MEMBER, StudyGroup.JoinStatus.PENDING);

        return studyGroupRepository.save(group);
    }

    @Transactional
    public StudyGroup approveJoinRequest(Long groupId, Long requestUserId, Long approverId) {
        StudyGroup group = findGroupById(groupId);
        User approver = findUserById(approverId);
        User requestUser = findUserById(requestUserId);

        if (!group.isLeader(approver) && !approver.equals(group.getMentor())) {
            throw new AccessDeniedException("가입 요청을 승인할 권한이 없습니다.");
        }

        // PENDING 상태인 요청 찾기
        UserStudyGroup request = group.getJoinRequests().stream()
                .filter(req -> req.getUser().equals(requestUser) &&
                        req.getStatus() == StudyGroup.JoinStatus.PENDING)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("가입 요청을 찾을 수 없습니다."));

        // 이미 승인된 멤버인지 확인
        boolean isAlreadyMember = group.getMembers().stream()
                .anyMatch(member -> member.getUser().equals(requestUser) &&
                        member.getStatus() == StudyGroup.JoinStatus.APPROVED);

        if (isAlreadyMember) {
            throw new InvalidOperationException("이미 승인된 멤버입니다.");
        }

        // 요청 승인 처리
        request.setStatus(StudyGroup.JoinStatus.APPROVED);
        request.setRole(UserStudyGroup.Role.MEMBER);

        // members 리스트에 추가
        if (!group.getMembers().contains(request)) {
            group.getMembers().add(request);
        }

        // joinRequests에서 제거
        group.getJoinRequests().remove(request);

        // 멤버 정리 및 수 재계산
        group.validateAndCleanupMembers();
        group.validateGroupStatus();  // 메서드추가용~

        // 멘토 처리
        if (requestUser.isMentor() && !group.hasMentor()) {
            group.setMentor(requestUser);
        }

        return studyGroupRepository.save(group);
    }

    @Transactional
    public StudyGroup rejectJoinRequest(Long groupId, Long requestUserId, Long rejecterId) {
        StudyGroup group = findGroupById(groupId);
        User rejecter = findUserById(rejecterId);
        User requestUser = findUserById(requestUserId);

        if (!group.isLeader(rejecter) && !rejecter.equals(group.getMentor())) {
            throw new AccessDeniedException("가입 요청을 거절할 권한이 없습니다.");
        }

        // 대기 중인 요청 찾기
        UserStudyGroup request = group.getJoinRequests().stream()
                .filter(req -> req.getUser().equals(requestUser) &&
                        req.getStatus() == StudyGroup.JoinStatus.PENDING)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("가입 요청을 찾을 수 없습니다."));

        // 상태를 REJECTED로 변경
        request.setStatus(StudyGroup.JoinStatus.REJECTED);

        // members 리스트에서 제거
        group.getMembers().removeIf(
                member -> member.getUser().equals(requestUser) &&
                        (member.getStatus() == StudyGroup.JoinStatus.PENDING ||
                                member.getStatus() == StudyGroup.JoinStatus.REJECTED)
        );

        // joinRequests에서도 제거
        group.getJoinRequests().removeIf(
                req -> req.getUser().equals(requestUser) &&
                        req.getStatus() == StudyGroup.JoinStatus.REJECTED
        );

        // 멤버 수 재계산
        group.validateAndCleanupMembers();

        return studyGroupRepository.save(group);
    }

    @Transactional
    public StudyGroup changeLeader(Long groupId, Long currentUserId, Long newLeaderId) {
        StudyGroup group = findGroupById(groupId);
        User currentUser = findUserById(currentUserId);
        User newLeader = findUserById(newLeaderId);

        if (!group.isLeader(currentUser)) {
            throw new AccessDeniedException("리더 권한을 위임할 권한이 없습니다.");
        }

        if (!group.isMember(newLeader)) {
            throw new InvalidOperationException("그룹 멤버에게만 리더 권한을 위임할 수 있습니다.");
        }

        UserStudyGroup currentLeaderMembership = group.getMembershipByUser(currentUser);
        UserStudyGroup newLeaderMembership = group.getMembershipByUser(newLeader);

        currentLeaderMembership.setRole(UserStudyGroup.Role.MEMBER);
        newLeaderMembership.setRole(UserStudyGroup.Role.LEADER);

        group.validateGroupStatus();
        return studyGroupRepository.save(group);
    }

    @Transactional
    public void leaveMember(Long groupId, Long userId) {
        StudyGroup group = findGroupById(groupId);
        User user = findUserById(userId);

        if (!group.isMember(user)) {
            throw new InvalidOperationException("해당 그룹의 멤버가 아닙니다.");
        }

        if (group.isLeader(user)) {
            throw new InvalidOperationException("리더는 권한을 위임한 후에 탈퇴할 수 있습니다.");
        }

        group.removeMember(user);

        if (user.equals(group.getMentor())) {
            group.setMentor(null);
        }

        group.getJoinRequests().removeIf(request -> request.getUser().equals(user));


        group.validateGroupStatus();
        studyGroupRepository.save(group);
    }

    @Transactional
    public StudyGroup matchUserToGroup(Long userId) {
        User user = findUserById(userId);
        String[] interestSkills = user.getInterestSkillsAsArray();

        if (interestSkills.length == 0) {
            throw new InvalidOperationException("관심 기술을 설정해주세요.");
        }

        List<StudyGroup> matchingGroups = studyGroupRepository.findAll().stream()
                .filter(StudyGroup::isRecruiting)
                .filter(group -> !group.isFull())
                .filter(group -> matchesInterestSkills(group, interestSkills))
                .toList();

        if (matchingGroups.isEmpty()) {
            throw new ResourceNotFoundException("매칭 가능한 그룹이 없습니다.");
        }

        StudyGroup selectedGroup = matchingGroups.get(0);
        return requestToJoin(selectedGroup.getId(), userId);
    }

    private StudyGroup findGroupById(Long groupId) {
        return studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("스터디 그룹을 찾을 수 없습니다."));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
    }

    private boolean matchesInterestSkills(StudyGroup group, String[] interestSkills) {
        for (String skill : interestSkills) {
            if ((group.getDescription() != null && group.getDescription().toLowerCase().contains(skill.toLowerCase())) ||
                    (group.getTitle() != null && group.getTitle().toLowerCase().contains(skill.toLowerCase())) ||
                    (group.getMentor() != null && group.getMentor().getTechStack() != null &&
                            group.getMentor().getTechStack().toLowerCase().contains(skill.toLowerCase()))) {
                return true;
            }
        }
        return false;
    }

    @Transactional(readOnly = true)
    public List<StudyGroup> getUserGroups(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }

        // 사용자가 멤버이거나 가입 요청한 모든 그룹 조회
        return studyGroupRepository.findUserRelatedGroups(userId);
    }


    @Transactional
    public StudyGroup requestMentorMatch(Long groupId, Long mentorId, Long requesterId) {
        StudyGroup group = findGroupById(groupId);
        User mentor = findUserById(mentorId);
        User requester = findUserById(requesterId);

        // 유효성 검사
        if (!group.isLeader(requester)) {
            throw new AccessDeniedException("그룹 리더만 멘토를 요청할 수 있습니다.");
        }
        if (!mentor.isMentor()) {
            throw new InvalidOperationException("멘토로 등록된 사용자만 멘토 요청을 받을 수 있습니다.");
        }
        if (group.getMentor() != null) {
            throw new InvalidOperationException("이미 멘토가 있는 그룹입니다.");
        }

        UserStudyGroup mentorRequest = UserStudyGroup.builder()
                .user(mentor)
                .studyGroup(group)
                .role(UserStudyGroup.Role.MENTOR)
                .status(StudyGroup.JoinStatus.MENTOR_PENDING)
                .build();

        group.getJoinRequests().add(mentorRequest);
        return studyGroupRepository.save(group);
    }

    @Transactional
    public StudyGroup approveMentorMatch(Long groupId, Long mentorId) {
        StudyGroup group = findGroupById(groupId);
        User mentor = findUserById(mentorId);

        // 요청 상태 확인
        UserStudyGroup request = group.getJoinRequests().stream()
                .filter(req -> req.getUser().equals(mentor)
                        && req.getStatus() == StudyGroup.JoinStatus.MENTOR_PENDING)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("멘토 요청을 찾을 수 없습니다."));

        // 상태를 APPROVED로 통합
        request.setStatus(StudyGroup.JoinStatus.APPROVED);
        group.setMentor(mentor);

        // 그룹 멤버로 추가
        if (!group.isMember(mentor)) {
            group.addMember(mentor, UserStudyGroup.Role.MENTOR, StudyGroup.JoinStatus.APPROVED);
        }

        // 요청 리스트에서 제거
        group.getJoinRequests().remove(request);

        return studyGroupRepository.save(group);
    }


    @Transactional
    public StudyGroup rejectMentorMatch(Long groupId, Long mentorId) {
        StudyGroup group = findGroupById(groupId);
        User mentor = findUserById(mentorId);

        UserStudyGroup request = group.getJoinRequests().stream()
                .filter(req -> req.getUser().equals(mentor)
                        && req.getStatus() == StudyGroup.JoinStatus.MENTOR_PENDING)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("멘토 요청을 찾을 수 없습니다."));

        request.setStatus(StudyGroup.JoinStatus.MENTOR_REJECTED);
        return studyGroupRepository.save(group);
    }




}
