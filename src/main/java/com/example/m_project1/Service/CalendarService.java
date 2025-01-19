package com.example.m_project1.Service;

import com.example.m_project1.dto.CreateCalendarRequest;
import com.example.m_project1.dto.UpdateCalendarRequest;
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
//보완필요 전체적으로
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarService {
    private final CalendarRepository calendarRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    @Transactional
    public Calendar createSchedule(Long userId, CreateCalendarRequest request) {
        User user = findUserById(userId);
        StudyGroup group = null;
        Task task = null;

        if (request.getGroupId() != null) {
            group = findStudyGroupById(request.getGroupId());
            if (!group.isMember(user)) {
                throw new AccessDeniedException("그룹의 멤버만 일정을 생성할 수 있습니다.");
            }
        }

        if (request.getTaskId() != null) {
            task = findTaskById(request.getTaskId());
            if (group != null && !task.getStudyGroup().equals(group)) {
                throw new InvalidOperationException("해당 그룹의 과제가 아닙니다.");
            }
        }

        Calendar schedule = Calendar.builder()
                .user(user)
                .studyGroup(group)
                .task(task)
                .title(request.getTitle())
                .content(request.getContent())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .type(determineScheduleType(group, task))
                .build();

        return calendarRepository.save(schedule);
    }

    @Transactional
    public Calendar updateSchedule(Long scheduleId, Long userId,  UpdateCalendarRequest request) {
        Calendar schedule = findCalendarById(scheduleId);
        User user = findUserById(userId);

        if (!schedule.canModify(user)) {
            throw new AccessDeniedException("일정을 수정할 권한이 없습니다.");
        }

        schedule.update(
                request.getTitle(),
                request.getContent(),
                request.getStartDate(),
                request.getEndDate()
        );

        if (request.getIsCompleted() != null) {
            if (request.getIsCompleted()) {
                schedule.complete();
            }
        }

        return calendarRepository.save(schedule);
    }

    @Transactional
    public void deleteSchedule(Long scheduleId, Long userId) {
        Calendar schedule = findCalendarById(scheduleId);
        User user = findUserById(userId);

        if (!schedule.canModify(user)) {
            throw new AccessDeniedException("일정을 삭제할 권한이 없습니다.");
        }

        calendarRepository.delete(schedule);
    }

    public List<Calendar> getUserSchedules(Long userId, LocalDateTime start, LocalDateTime end) {
        validateDateRange(start, end);
        return calendarRepository.findByUser_UserIdAndStartDateBetweenOrderByStartDateAsc(
                userId, start, end
        );
    }

    public List<Calendar> getGroupSchedules(Long groupId, Long userId, LocalDateTime start,
                                            LocalDateTime end) {
        validateDateRange(start, end);

        StudyGroup group = findStudyGroupById(groupId);
        User user = findUserById(userId);

        if (!group.isMember(user)) {
            throw new AccessDeniedException("그룹의 멤버만 일정을 조회할 수 있습니다.");
        }

        return calendarRepository.findByStudyGroupIdAndStartDateBetweenOrderByStartDateAsc(
                groupId, start, end
        );
    }

    private Calendar.ScheduleType determineScheduleType(StudyGroup group, Task task) {
        if (task != null) return Calendar.ScheduleType.TASK;
        if (group != null) return Calendar.ScheduleType.GROUP;
        return Calendar.ScheduleType.PERSONAL;
    }

    private void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("시작일은 종료일 이전이어야 합니다.");
        }
    }

    private Calendar findCalendarById(Long calendarId) {
        return calendarRepository.findById(calendarId)
                .orElseThrow(() -> new ResourceNotFoundException("일정을 찾을 수 없습니다."));
    }

    private StudyGroup findStudyGroupById(Long groupId) {
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
    public void completeTaskSchedules(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("과제를 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        if (!task.getStudyGroup().getMentor().equals(user)) {
            throw new AccessDeniedException("멘토만 과제 일정을 완료 처리할 수 있습니다.");
        }

        List<Calendar> schedules = calendarRepository.findByTaskId(taskId);
        schedules.forEach(Calendar::complete);
        calendarRepository.saveAll(schedules);
    }

}