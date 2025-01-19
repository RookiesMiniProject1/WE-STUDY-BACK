package com.example.m_project1.entity;

import com.example.m_project1.exception.InvalidOperationException;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "study_group")
public class StudyGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id")
    private User mentor;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "max_members", nullable = false)
    private int maxMembers = 10;

    @Column(name = "current_member", nullable = false)
    private int currentMember;

    @Column(name = "is_recruiting", nullable = false)
    private boolean isRecruiting = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.PREPARING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "studyGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserStudyGroup> members = new ArrayList<>();

    @OneToMany(mappedBy = "studyGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserStudyGroup> joinRequests = new ArrayList<>();

    @OneToMany(mappedBy = "studyGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Board> boards = new ArrayList<>();

    @OneToMany(mappedBy = "studyGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "studyGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Calendar> schedules = new ArrayList<>();

    @OneToOne(mappedBy = "studyGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private KanbanBoard kanbanBoard;

    public enum Status {
        PREPARING,
        ONGOING,
        COMPLETED
    }

    // StudyGroup.java
    public enum JoinStatus {
        PENDING,         // 일반 가입 대기
        APPROVED,        // 승인됨
        REJECTED,        // 거절됨
        MENTOR_PENDING,  // 멘토 매칭 대기 추가
        MENTOR_APPROVED, // 멘토 매칭 승인 추가
        MENTOR_REJECTED  // 멘토 매칭 거절 추가
    }

    public void validateAndCleanupMembers() {
        // members 리스트에서 중복 제거
        Set<UserStudyGroup> uniqueMembers = new LinkedHashSet<>(this.members);
        this.members.clear();
        this.members.addAll(uniqueMembers);

        // 실제 승인된 멤버 수 재계산
        this.currentMember = (int) this.members.stream()
                .filter(member -> member.getStatus() == JoinStatus.APPROVED)
                .count();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isFull() {
        return this.currentMember >= this.maxMembers;
    }

    public boolean hasMentor() {
        return this.mentor != null;
    }

    public void addMember(User user, UserStudyGroup.Role role, StudyGroup.JoinStatus status) {
        // 중복 체크 로직 강화
        boolean exists = this.members.stream()
                .anyMatch(member ->
                        member.getUser().equals(user) &&
                                (member.getStatus() == JoinStatus.APPROVED ||
                                        member.getStatus() == JoinStatus.PENDING)
                );

        if (exists) {
            throw new InvalidOperationException("이미 존재하는 멤버입니다.");
        }

        UserStudyGroup membership = UserStudyGroup.builder()
                .user(user)
                .studyGroup(this)
                .role(role)
                .status(status)
                .build();

        // PENDING 상태는 오직 joinRequests에만 추가
        if (status == JoinStatus.PENDING) {
            this.joinRequests.add(membership);
        }
        // REJECTED 상태는 어디에도 추가하지 않음
        else if (status == JoinStatus.APPROVED) {
            this.members.add(membership);
            this.currentMember++;
        }
    }

    public void removeMember(User user) {
        this.members.removeIf(member -> member.getUser().equals(user));
        this.currentMember--;
    }

    public boolean isMember(User user) {
        return this.members.stream()
                .anyMatch(member -> member.getUser().equals(user) &&
                        member.getStatus() == JoinStatus.APPROVED);
    }

    public boolean isLeader(User user) {
        return this.members.stream()
                .anyMatch(member -> member.getUser().equals(user) &&
                        member.getRole() == UserStudyGroup.Role.LEADER &&
                        member.getStatus() == JoinStatus.APPROVED);
    }

    public UserStudyGroup getMembershipByUser(User user) {
        return this.members.stream()
                .filter(member -> member.getUser().equals(user))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("멤버를 찾을 수 없습니다."));
    }


    @OneToMany(mappedBy = "studyGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatRoom> chatRooms = new ArrayList<>();

    @Builder
    public StudyGroup(String title, String description, User mentor, int maxMembers,int currentMember, boolean isRecruiting) {
        this.title = Objects.requireNonNull(title, "제목은 필수입니다.");
        this.description = description;
        this.mentor = mentor;
        this.maxMembers = maxMembers;
        this.currentMember = currentMember;
        this.isRecruiting = isRecruiting;


        // 그룹 생성 시 기본 채팅방 자동 생성
        ChatRoom defaultChatRoom = ChatRoom.createDefaultRoom(this);
        this.chatRooms.add(defaultChatRoom);
    }

    // 추가 채팅방 생성 메서드
    public ChatRoom createChatRoom(String roomName, User creator) {
        if (!isMember(creator)) {
            throw new IllegalStateException("그룹 멤버만 채팅방을 생성할 수 있습니다.");
        }

        ChatRoom chatRoom = ChatRoom.createAdditionalRoom(this, roomName);
        this.chatRooms.add(chatRoom);
        return chatRoom;
    }

    // 채팅방 삭제 메서드
    public void deleteChatRoom(ChatRoom chatRoom, User user) {
        if (!chatRoom.canDelete(user)) {
            throw new IllegalStateException("채팅방을 삭제할 권한이 없거나, 기본 채팅방은 삭제할 수 없습니다.");
        }

        this.chatRooms.remove(chatRoom);
    }


    // 기본 채팅방 조회
    public ChatRoom getDefaultChatRoom() {
        return this.chatRooms.stream()
                .filter(ChatRoom::isDefault)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("기본 채팅방이 존재하지 않습니다."));
    }

    // 그룹의 모든 채팅방 조회
    public List<ChatRoom> getAllChatRooms() {
        return Collections.unmodifiableList(this.chatRooms);
    }

    public void addInitialMember(User user, UserStudyGroup.Role role) {
        UserStudyGroup membership = UserStudyGroup.builder()
                .user(user)
                .studyGroup(this)
                .role(role)
                .status(StudyGroup.JoinStatus.APPROVED)  // 초기 멤버는 무조건 APPROVED
                .build();

        this.members.add(membership);
        this.currentMember++;
    }

    public boolean canChangeMentor() {
        return this.mentor == null || this.getMembers().isEmpty();
    }

    public void setMentor(User mentor) {
        if (mentor != null && !mentor.isMentor()) {
            throw new InvalidOperationException("멘토 권한이 없는 사용자입니다.");
        }
        this.mentor = mentor;
    }
    public void validateGroupStatus() {
        if (this.currentMember != this.getMembers().stream()
                .filter(m -> m.getStatus() == JoinStatus.APPROVED)
                .count()) {
            throw new InvalidOperationException("멤버 수가 일치하지 않습니다.");
        }

        if (this.currentMember > this.maxMembers) {
            throw new InvalidOperationException("현재 멤버 수가 최대 멤버 수를 초과했습니다.");
        }

        // 리더가 한 명인지 확인
        long leaderCount = this.getMembers().stream()
                .filter(m -> m.getRole() == UserStudyGroup.Role.LEADER &&
                        m.getStatus() == JoinStatus.APPROVED)
                .count();
        if (leaderCount != 1) {
            throw new InvalidOperationException("그룹에는 정확히 한 명의 리더가 있어야 합니다.");
        }
    }
}
