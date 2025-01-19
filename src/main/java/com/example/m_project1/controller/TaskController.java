package com.example.m_project1.controller;

import com.example.m_project1.Service.TaskService;
import com.example.m_project1.dto.*;
import com.example.m_project1.entity.Task;
import com.example.m_project1.entity.TaskSubmission;
import com.example.m_project1.entity.User;
import com.example.m_project1.exception.InvalidOperationException;
import com.example.m_project1.repository.UserRepository;
import com.example.m_project1.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/groups/{groupId}/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> createTask(
            @PathVariable Long groupId,
            @Valid @RequestBody CreateTaskRequest request,
            Authentication authentication) {
        try {
            Long mentorId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
            Task task = taskService.createTask(groupId, mentorId, request);
            return ResponseEntity.ok(new TaskResponse(task));
        } catch (Exception e) {
            log.error("과제 생성 중 오류 발생: ", e);
            throw new InvalidOperationException(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getGroupTasks(
            @PathVariable Long groupId,
            Authentication authentication) {
        Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        List<Task> tasks = taskService.getGroupTasks(groupId, userId);
        List<TaskResponse> response = tasks.stream()
                .map(TaskResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<?> getTask(
            @PathVariable Long groupId,
            @PathVariable Long taskId,
            Authentication authentication) {
        Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        Task task = taskService.getTask(taskId, userId);
        return ResponseEntity.ok(new TaskResponse(task));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<?> updateTask(
            @PathVariable Long groupId,
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request,
            Authentication authentication) {
        Long mentorId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        Task task = taskService.updateTask(taskId, mentorId, request);
        return ResponseEntity.ok(new TaskResponse(task));
    }

    @PutMapping("/{taskId}/status")
    public ResponseEntity<?> updateTaskStatus(
            @PathVariable Long groupId,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskStatusRequest request,
            Authentication authentication) {
        Long mentorId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        Task task = taskService.updateTaskStatus(taskId, mentorId, request.getStatus());
        return ResponseEntity.ok(new TaskResponse(task));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<?> deleteTask(
            @PathVariable Long groupId,
            @PathVariable Long taskId,
            Authentication authentication) {
        Long mentorId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        taskService.deleteTask(taskId, mentorId);
        return ResponseEntity.ok().build();
    }
}
