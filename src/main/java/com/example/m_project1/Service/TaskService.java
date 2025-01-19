package com.example.m_project1.Service;

import com.example.m_project1.dto.*;
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

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskSubmissionRepository submissionRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final UserRepository userRepository;
    private final KanbanItemService kanbanItemService;
    private final CalendarRepository calendarRepository;

    @Transactional
    public Task createTask(Long groupId, Long mentorId, CreateTaskRequest request) {
        StudyGroup group = findStudyGroupById(groupId);
        User mentor = findUserById(mentorId);

        if (!mentor.equals(group.getMentor())) {
            throw new AccessDeniedException("멘토만 과제를 생성할 수 있습니다.");
        }

        if (taskRepository.existsByStudyGroupIdAndTitle(groupId, request.getTitle())) {
            throw new InvalidOperationException("이미 존재하는 과제 제목입니다.");
        }

        Task task = Task.builder()
                .studyGroup(group)
                .title(request.getTitle())
                .description(request.getDescription())
                .deadline(request.getDeadline())
                .build();

        Task savedTask = taskRepository.save(task);

        // 과제 생성 시 자동으로 일정 생성
        Calendar taskSchedule = Calendar.builder()
                .title("[과제] " + request.getTitle())
                .content(request.getDescription())
                .startDate(LocalDateTime.now())
                .endDate(request.getDeadline())
                .type(Calendar.ScheduleType.TASK)
                .user(mentor)
                .studyGroup(group)
                .task(savedTask)
                .build();

        calendarRepository.save(taskSchedule);

        return savedTask;
    }

    @Transactional
    public Task updateTask(Long taskId, Long mentorId, UpdateTaskRequest request) {
        Task task = findTaskById(taskId);
        User mentor = findUserById(mentorId);

        if (!mentor.equals(task.getStudyGroup().getMentor())) {
            throw new AccessDeniedException("멘토만 과제를 수정할 수 있습니다.");
        }

        task.update(request.getTitle(), request.getDescription(), request.getDeadline());

        // 관련된 일정도 업데이트
        List<Calendar> schedules = calendarRepository.findByTaskId(taskId);
        for (Calendar schedule : schedules) {
            schedule.update(
                    "[과제] " + request.getTitle(),
                    request.getDescription(),
                    null,
                    request.getDeadline()
            );
        }
        calendarRepository.saveAll(schedules);

        return taskRepository.save(task);
    }

    @Transactional
    public Task updateTaskStatus(Long taskId, Long mentorId, Task.Status newStatus) {
        Task task = findTaskById(taskId);
        User mentor = findUserById(mentorId);

        if (!mentor.equals(task.getStudyGroup().getMentor())) {
            throw new AccessDeniedException("멘토만 과제 상태를 변경할 수 있습니다.");
        }

        task.updateStatus(newStatus);

        // 연관된 칸반 아이템 상태 업데이트
        kanbanItemService.syncWithTask(task.getId(), newStatus);

        // 완료 상태가 되면 관련 일정도 완료 처리
        if (newStatus == Task.Status.COMPLETED) {
            List<Calendar> schedules = calendarRepository.findByTaskId(taskId);
            schedules.forEach(Calendar::complete);
            calendarRepository.saveAll(schedules);
        }

        return taskRepository.save(task);
    }

    @Transactional
    public void deleteTask(Long taskId, Long mentorId) {
        Task task = findTaskById(taskId);
        User mentor = findUserById(mentorId);

        if (!mentor.equals(task.getStudyGroup().getMentor())) {
            throw new AccessDeniedException("멘토만 과제를 삭제할 수 있습니다.");
        }

        // 연관된 데이터 삭제
        submissionRepository.deleteByTaskId(taskId);
        calendarRepository.deleteByTaskId(taskId);
        kanbanItemService.deleteByTaskId(taskId);
        taskRepository.delete(task);
    }

    public List<Task> getGroupTasks(Long groupId, Long userId) {
        StudyGroup group = findStudyGroupById(groupId);
        User user = findUserById(userId);

        if (!group.isMember(user)) {
            throw new AccessDeniedException("그룹 멤버만 과제를 조회할 수 있습니다.");
        }

        return taskRepository.findByStudyGroupIdOrderByDeadlineAsc(groupId);
    }

    public Task getTask(Long taskId, Long userId) {
        Task task = findTaskById(taskId);
        User user = findUserById(userId);

        if (!task.getStudyGroup().isMember(user)) {
            throw new AccessDeniedException("그룹 멤버만 과제를 조회할 수 있습니다.");
        }

        return task;
    }

    private Task findTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("과제를 찾을 수 없습니다."));
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
