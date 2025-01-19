package com.example.m_project1.controller;

import com.example.m_project1.Service.TaskSubmissionService;
import com.example.m_project1.dto.MentorFeedbackRequest;
import com.example.m_project1.dto.PeerFeedbackRequest;
import com.example.m_project1.dto.SubmitTaskRequest;
import com.example.m_project1.dto.TaskSubmissionResponse;
import com.example.m_project1.entity.TaskSubmission;
import com.example.m_project1.exception.InvalidOperationException;
import com.example.m_project1.repository.UserRepository;
import com.example.m_project1.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/groups/{groupId}/tasks/{taskId}/submissions")
@RequiredArgsConstructor
public class TaskSubmissionController {
    private final TaskSubmissionService submissionService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> submitTask(
            @PathVariable Long groupId,
            @PathVariable Long taskId,
            @Valid @RequestBody SubmitTaskRequest request,
            Authentication authentication) {
        try {
            Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
            TaskSubmission submission = submissionService.submitTask(taskId, userId, request);
            return ResponseEntity.ok(new TaskSubmissionResponse(submission));
        } catch (Exception e) {
            log.error("과제 제출 중 오류 발생: ", e);
            throw new InvalidOperationException(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getSubmissions(
            @PathVariable Long groupId,
            @PathVariable Long taskId,
            Authentication authentication) {
        Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        List<TaskSubmission> submissions = submissionService.getTaskSubmissions(taskId, userId);
        List<TaskSubmissionResponse> response = submissions.stream()
                .map(TaskSubmissionResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{submissionId}")
    public ResponseEntity<?> getSubmission(
            @PathVariable Long groupId,
            @PathVariable Long taskId,
            @PathVariable Long submissionId,
            Authentication authentication) {
        Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        TaskSubmission submission = submissionService.getSubmission(submissionId, userId);
        return ResponseEntity.ok(new TaskSubmissionResponse(submission));
    }

    @PostMapping("/{submissionId}/mentor-feedback")
    public ResponseEntity<?> addMentorFeedback(
            @PathVariable Long groupId,
            @PathVariable Long taskId,
            @PathVariable Long submissionId,
            @Valid @RequestBody MentorFeedbackRequest request,
            Authentication authentication) {
        Long mentorId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        TaskSubmission submission = submissionService.addMentorFeedback(submissionId, mentorId, request);
        return ResponseEntity.ok(new TaskSubmissionResponse(submission));
    }

    @PostMapping("/{submissionId}/peer-feedback")
    public ResponseEntity<?> addPeerFeedback(
            @PathVariable Long groupId,
            @PathVariable Long taskId,
            @PathVariable Long submissionId,
            @Valid @RequestBody PeerFeedbackRequest request,
            Authentication authentication) {
        Long reviewerId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        TaskSubmission submission = submissionService.addPeerFeedback(submissionId, reviewerId, request);
        return ResponseEntity.ok(new TaskSubmissionResponse(submission));
    }
}