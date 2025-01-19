package com.example.m_project1.Service;

import com.example.m_project1.dto.MentorFeedbackRequest;
import com.example.m_project1.dto.PeerFeedbackRequest;
import com.example.m_project1.dto.SubmitTaskRequest;
import com.example.m_project1.entity.StudyGroup;
import com.example.m_project1.entity.Task;
import com.example.m_project1.entity.TaskSubmission;
import com.example.m_project1.entity.User;
import com.example.m_project1.exception.AccessDeniedException;
import com.example.m_project1.exception.InvalidOperationException;
import com.example.m_project1.exception.ResourceNotFoundException;
import com.example.m_project1.repository.TaskRepository;
import com.example.m_project1.repository.TaskSubmissionRepository;
import com.example.m_project1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskSubmissionService {
    private final TaskSubmissionRepository submissionRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final KanbanItemService kanbanItemService;

    @Transactional
    public TaskSubmission submitTask(Long taskId, Long userId, SubmitTaskRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("과제를 찾을 수 없습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        // 승인된 멤버인지 확인
        if (task.getStudyGroup().getMembers().stream()
                .noneMatch(member -> member.getUser().equals(user) &&
                        member.getStatus() == StudyGroup.JoinStatus.APPROVED)) {
            throw new AccessDeniedException("승인된 그룹 멤버만 과제를 제출할 수 있습니다.");
        }

        if (task.isOverdue()) {
            throw new InvalidOperationException("제출 기한이 지났습니다.");
        }

        // 기존 제출물 확인 (재제출 처리)
        Optional<TaskSubmission> existingSubmission =
                submissionRepository.findByTask_IdAndUser_UserId(taskId, userId);

        if (existingSubmission.isPresent()) {
            TaskSubmission submission = existingSubmission.get();
            submission.update(request.getContent(), request.getFilePath(), request.getFileName());
            return submissionRepository.save(submission);
        }

        // 새 제출물 생성
        TaskSubmission submission = TaskSubmission.builder()
                .task(task)
                .user(user)
                .content(request.getContent())
                .filePath(request.getFilePath())
                .fileName(request.getFileName())
                .build();

        TaskSubmission savedSubmission = submissionRepository.save(submission);

        // 연관된 칸반 아이템 상태 업데이트
        kanbanItemService.updateSubmissionStatus(taskId, userId);

        return savedSubmission;
    }

    @Transactional
    public TaskSubmission addMentorFeedback(Long submissionId, Long mentorId, MentorFeedbackRequest request) {
        TaskSubmission submission = findSubmissionById(submissionId);
        User mentor = findUserById(mentorId);
        Task task = submission.getTask();

        if (!mentor.equals(task.getStudyGroup().getMentor())) {
            throw new AccessDeniedException("멘토만 피드백을 작성할 수 있습니다.");
        }

        submission.updateMentorFeedback(request.getFeedback(), request.getScore());

        // 높은 점수를 받은 경우 칸반 아이템 완료 처리
        if (request.getScore() != null && request.getScore() >= 80) {
            kanbanItemService.updateItemStatusToComplete(task.getId(), submission.getUser().getUserId());
        }

        return submissionRepository.save(submission);
    }

    @Transactional
    public TaskSubmission addPeerFeedback(Long submissionId, Long reviewerId, PeerFeedbackRequest request) {
        TaskSubmission submission = findSubmissionById(submissionId);
        User reviewer = findUserById(reviewerId);
        Task task = submission.getTask();

        if (submission.getUser().equals(reviewer)) {
            throw new InvalidOperationException("자신의 과제는 평가할 수 없습니다.");
        }

        if (!task.getStudyGroup().isMember(reviewer)) {
            throw new AccessDeniedException("그룹 멤버만 피어 리뷰를 작성할 수 있습니다.");
        }

        if (!task.isOverdue()) {
            throw new InvalidOperationException("과제 마감 후에만 피어 리뷰가 가능합니다.");
        }

        submission.updatePeerFeedback(request.getFeedback(), request.getScore());
        return submissionRepository.save(submission);
    }

    public List<TaskSubmission> getTaskSubmissions(Long taskId, Long userId) {
        Task task = findTaskById(taskId);
        User user = findUserById(userId);
        StudyGroup group = task.getStudyGroup();

        if (!group.isMember(user)) {
            throw new AccessDeniedException("그룹 멤버만 제출물을 조회할 수 있습니다.");
        }

        // 멘토는 모든 제출물을, 멤버는 마감 후에만 다른 멤버의 제출물을 볼 수 있음
        if (!user.equals(group.getMentor()) && !task.isOverdue()) {
            return submissionRepository.findByTask_IdAndUser_UserId(taskId, userId)
                    .map(List::of)
                    .orElse(Collections.emptyList());
        }

        return submissionRepository.findByTaskId(taskId);
    }

    public TaskSubmission getSubmission(Long submissionId, Long userId) {
        TaskSubmission submission = findSubmissionById(submissionId);
        User user = findUserById(userId);
        StudyGroup group = submission.getTask().getStudyGroup();

        if (!group.isMember(user)) {
            throw new AccessDeniedException("그룹 멤버만 제출물을 조회할 수 있습니다.");
        }

        // 본인 제출물이 아니고, 멘토도 아니며, 마감 전인 경우 접근 불가
        if (!submission.getUser().equals(user) &&
                !user.equals(group.getMentor()) &&
                !submission.getTask().isOverdue()) {
            throw new AccessDeniedException("다른 멤버의 제출물은 마감 후에만 조회할 수 있습니다.");
        }

        return submission;
    }

    private TaskSubmission findSubmissionById(Long submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("제출물을 찾을 수 없습니다."));
    }

    private Task findTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("과제를 찾을 수 없습니다."));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
    }
}
