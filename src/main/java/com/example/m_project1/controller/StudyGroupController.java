package com.example.m_project1.controller;

import com.example.m_project1.dto.*;
import com.example.m_project1.entity.StudyGroup;
import com.example.m_project1.Service.StudyGroupService;
import com.example.m_project1.entity.User;
import com.example.m_project1.entity.UserStudyGroup;
import com.example.m_project1.exception.AccessDeniedException;
import com.example.m_project1.exception.InvalidOperationException;
import com.example.m_project1.exception.ResourceNotFoundException;
import com.example.m_project1.repository.UserStudyGroupRepository;
import com.example.m_project1.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;  // Authentication import
import com.example.m_project1.repository.UserRepository; // UserRepository import

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class StudyGroupController {

    private final StudyGroupService studyGroupService;
    private final UserRepository userRepository;
    private final UserStudyGroupRepository userStudyGroupRepository;

    @PostMapping
    public ResponseEntity<?> createGroup(
            @Valid @RequestBody CreateStudyGroupRequest request,
            Authentication authentication) {
        try {
            Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
            User currentUser = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
            StudyGroup studyGroup = studyGroupService.createStudyGroup(
                    request.getTitle(),
                    request.getDescription(),
                    userId,
                    request.getMaxMembers()
            );
            return ResponseEntity.ok(new StudyGroupResponseDto(studyGroup, currentUser));  // currentUser 추가
        } catch (Exception e) {
            log.error("Error creating study group: ", e);
            throw new InvalidOperationException(e.getMessage());
        }
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<?> getStudyGroup(
            @PathVariable Long groupId,
            Authentication authentication) {
        Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        StudyGroup studyGroup = studyGroupService.getStudyGroup(groupId);
        return ResponseEntity.ok(new StudyGroupResponseDto(studyGroup, currentUser));  // currentUser 추가
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<?> updateGroup(
            @PathVariable Long groupId,
            @Valid @RequestBody UpdateStudyGroupRequest request,
            Authentication authentication) {
        Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        StudyGroup updatedGroup = studyGroupService.updateStudyGroup(
                groupId,
                request.getTitle(),
                request.getDescription(),
                userId,
                request.getMaxMembers(),
                request.getIsRecruiting()
        );
        return ResponseEntity.ok(new StudyGroupResponseDto(updatedGroup, currentUser));  // currentUser 추가
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<?> deleteGroup(
            @PathVariable Long groupId,
            Authentication authentication) {
        Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        studyGroupService.deleteStudyGroup(groupId, userId);
        return ResponseEntity.ok("스터디 그룹이 삭제되었습니다.");
    }

    @GetMapping
    public ResponseEntity<?> getAllGroups(Authentication authentication) {
        Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        List<StudyGroup> studyGroups = studyGroupService.getAllStudyGroups();
        List<StudyGroupResponseDto> responseDtos = studyGroups.stream()
                .map(group -> new StudyGroupResponseDto(group, currentUser))  // currentUser 추가
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    @PostMapping("/{groupId}/join/request")
    public ResponseEntity<?> requestToJoinGroup(
            @PathVariable Long groupId,
            Authentication authentication) {
        Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        StudyGroup group = studyGroupService.requestToJoin(groupId, userId);
        return ResponseEntity.ok(new StudyGroupResponseDto(group, currentUser));  // currentUser 추가
    }

    @PostMapping("/{groupId}/join/{requestUserId}/approve")
    public ResponseEntity<?> approveJoinRequest(
            @PathVariable Long groupId,
            @PathVariable Long requestUserId,
            Authentication authentication) {
        Long approverId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        User currentUser = userRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        StudyGroup group = studyGroupService.approveJoinRequest(groupId, requestUserId, approverId);
        return ResponseEntity.ok(new StudyGroupResponseDto(group, currentUser));  // currentUser 추가
    }

    @PostMapping("/{groupId}/join/{requestUserId}/reject")
    public ResponseEntity<?> rejectJoinRequest(
            @PathVariable Long groupId,
            @PathVariable Long requestUserId,
            Authentication authentication) {
        Long rejecterId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        User currentUser = userRepository.findById(rejecterId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        StudyGroup group = studyGroupService.rejectJoinRequest(groupId, requestUserId, rejecterId);
        return ResponseEntity.ok(new StudyGroupResponseDto(group, currentUser));  // currentUser 추가
    }

    @PutMapping("/{groupId}/leader")
    public ResponseEntity<?> changeLeader(
            @PathVariable Long groupId,
            @RequestBody ChangeLeaderRequest request,
            Authentication authentication) {
        Long currentUserId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        StudyGroup group = studyGroupService.changeLeader(groupId, currentUserId, request.getNewLeaderId());
        return ResponseEntity.ok(new StudyGroupResponseDto(group, currentUser));  // currentUser 추가
    }

    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<?> leaveMember(
            @PathVariable Long groupId,
            Authentication authentication) {
        Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        studyGroupService.leaveMember(groupId, userId);
        return ResponseEntity.ok("그룹에서 탈퇴되었습니다.");
    }

    @PostMapping("/match")
    public ResponseEntity<?> matchUser(Authentication authentication) {
        try {
            Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
            User currentUser = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
            StudyGroup matchedGroup = studyGroupService.matchUserToGroup(userId);
            return ResponseEntity.ok(new StudyGroupResponseDto(matchedGroup, currentUser));  // currentUser 추가
        } catch (IllegalStateException e) {
            log.warn("매칭 실패: {}", e.getMessage());
            throw new ResourceNotFoundException("조건에 맞는 그룹이 없습니다.");
        }
    }

    //내가 신청한 그룹, 속한 그룹 다 보는 엔드포인트
    //신청,모집중 등 필터링 해서 보는 기능 추가
    @GetMapping("/my-groups")
    public ResponseEntity<?> getUserGroups(
            Authentication authentication,
            @RequestParam(required = false) StudyGroup.JoinStatus status) {

        Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        if (status == null) {
            // 내가 신청한 그룹, 속한 그룹 모두 조회
            List<StudyGroup> userGroups = studyGroupService.getUserGroups(userId);
            List<StudyGroupResponseDto> responseDtos = userGroups.stream()
                    .map(group -> new StudyGroupResponseDto(group, currentUser))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responseDtos);
        } else {
            // 상태별 필터링 조회
            List<UserStudyGroup> userGroups = userStudyGroupRepository.findByUser_UserIdAndStatus(userId, status);
            List<StudyGroupResponseDto> responseDtos = userGroups.stream()
                    .map(userGroup -> new StudyGroupResponseDto(userGroup.getStudyGroup(), currentUser))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responseDtos);
        }
    }


    //멘토에게 매칭 요청
    @PostMapping("/{groupId}/mentor-request/{mentorId}")
    public ResponseEntity<?> requestMentorMatch(
            @PathVariable Long groupId,
            @PathVariable Long mentorId,
            Authentication authentication) {
        Long requesterId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        User currentUser = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        StudyGroup group = studyGroupService.requestMentorMatch(groupId, mentorId, requesterId);
        return ResponseEntity.ok(new StudyGroupResponseDto(group, currentUser));
    }

    //멘토가 수락
    @PostMapping("/{groupId}/mentor-request/{mentorId}/approve")
    public ResponseEntity<?> approveMentorMatch(
            @PathVariable Long groupId,
            @PathVariable Long mentorId,
            Authentication authentication) {
        Long approverId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        User currentUser = userRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        if (!approverId.equals(mentorId)) {
            throw new AccessDeniedException("멘토 본인만 요청을 승인할 수 있습니다.");
        }
        StudyGroup group = studyGroupService.approveMentorMatch(groupId, mentorId);
        return ResponseEntity.ok(new StudyGroupResponseDto(group, currentUser));
    }

    //멘토가 거절
    @PostMapping("/{groupId}/mentor-request/{mentorId}/reject")
    public ResponseEntity<?> rejectMentorMatch(
            @PathVariable Long groupId,
            @PathVariable Long mentorId,
            Authentication authentication) {
        Long rejecterId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        User currentUser = userRepository.findById(rejecterId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        if (!rejecterId.equals(mentorId)) {
            throw new AccessDeniedException("멘토 본인만 요청을 거절할 수 있습니다.");
        }
        StudyGroup group = studyGroupService.rejectMentorMatch(groupId, mentorId);
        return ResponseEntity.ok(new StudyGroupResponseDto(group, currentUser));
    }

    /*@GetMapping("/users")
    public ResponseEntity<?> getAllUsers(Authentication authentication) {
        List<User> users = userRepository.findAllUsers();
        return ResponseEntity.ok(users.stream()
                .map(user -> new UserDto(
                        user.getUserId(),
                        user.getEmail(),
                        user.getRole(),
                        user.getCareer(),
                        user.getTechStack()
                ))
                .collect(Collectors.toList()));
    }*/ //이건 보류. 모든 유저 조회는 필요없을듯

    @GetMapping("/mentors")
    public ResponseEntity<?> getAllMentors(Authentication authentication) {
        List<User> mentors = userRepository.findByRole(User.Role.MENTOR);
        List<UserProfileResponse> response = mentors.stream()
                .map(mentor -> UserProfileResponse.forMentor(
                        mentor.getUserId(),
                        mentor.getEmail(),
                        mentor.getRole().name(),
                        mentor.getCareer(),
                        mentor.getTechStack()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }



}
