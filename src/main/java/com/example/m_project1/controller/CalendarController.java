package com.example.m_project1.controller;

import com.example.m_project1.Service.CalendarService;
import com.example.m_project1.dto.CalendarDto;
import com.example.m_project1.dto.CalendarResponse;
import com.example.m_project1.dto.CreateCalendarRequest;
import com.example.m_project1.dto.UpdateCalendarRequest;
import com.example.m_project1.entity.Calendar;
import com.example.m_project1.entity.User;
import com.example.m_project1.exception.InvalidOperationException;
import com.example.m_project1.repository.UserRepository;
import com.example.m_project1.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {
    private final CalendarService calendarService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> createSchedule(
            @Valid @RequestBody CreateCalendarRequest request,
            Authentication authentication) {
        try {
            Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
            Calendar schedule = calendarService.createSchedule(userId, request);
            return ResponseEntity.ok(new CalendarResponse(schedule));
        } catch (Exception e) {
            log.error("일정 생성 중 오류 발생: ", e);
            throw new InvalidOperationException(e.getMessage());
        }
    }

    @GetMapping("/personal")
    public ResponseEntity<?> getUserSchedules(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            Authentication authentication) {
        Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        List<Calendar> schedules = calendarService.getUserSchedules(userId, start, end);
        List<CalendarResponse> response = schedules.stream()
                .map(CalendarResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<?> getGroupSchedules(
            @PathVariable Long groupId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            Authentication authentication) {
        Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        List<Calendar> schedules = calendarService.getGroupSchedules(groupId, userId, start, end);
        List<CalendarResponse> response = schedules.stream()
                .map(CalendarResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{scheduleId}")
    public ResponseEntity<?> updateSchedule(
            @PathVariable Long scheduleId,
            @Valid @RequestBody UpdateCalendarRequest request,
            Authentication authentication) {
        Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        Calendar schedule = calendarService.updateSchedule(scheduleId, userId, request);
        return ResponseEntity.ok(new CalendarResponse(schedule));
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<?> deleteSchedule(
            @PathVariable Long scheduleId,
            Authentication authentication) {
        Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
        calendarService.deleteSchedule(scheduleId, userId);
        return ResponseEntity.ok().build();
    }

    // 전체 일정 조회 (개인 + 그룹)
    @GetMapping
    public ResponseEntity<?> getAllSchedules(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(required = false) Long groupId,
            Authentication authentication) {
        try {
            Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
            List<Calendar> schedules;

            if (groupId != null) {
                schedules = calendarService.getGroupSchedules(groupId, userId, start, end);
            } else {
                schedules = calendarService.getUserSchedules(userId, start, end);
            }

            List<CalendarResponse> response = schedules.stream()
                    .map(CalendarResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("일정 조회 중 오류 발생: ", e);
            throw new InvalidOperationException(e.getMessage());
        }
    }

    // Task 상태가 변경될 때 관련된 일정의 완료 상태도 업데이트
    @PutMapping("/task/{taskId}/complete")
    public ResponseEntity<?> completeTaskSchedules(
            @PathVariable Long taskId,
            Authentication authentication) {
        try {
            Long userId = SecurityUtils.getUserIdFromAuth(authentication, userRepository);
            calendarService.completeTaskSchedules(taskId, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("과제 일정 완료 처리 중 오류 발생: ", e);
            throw new InvalidOperationException(e.getMessage());
        }
    }
}