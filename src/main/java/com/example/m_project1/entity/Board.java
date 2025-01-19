package com.example.m_project1.entity;

import com.example.m_project1.dto.BoardDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "board")
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private StudyGroup studyGroup;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BoardType boardType;

    @Column(nullable = false)
    private LocalDateTime postTime;

    @Column(nullable = false)
    private LocalDateTime editTime;



    @Getter
    public enum BoardType {
        NOTICE("공지사항"),            // 전체 공지
        DISCUSSION("자유게시판"),      // 전체 자유게시판
        STUDY_MATCHING("스터디매칭"), // 스터디/멘토 매칭
        PORTFOLIO("포트폴리오"),      // 포트폴리오
        GROUP_NOTICE("그룹공지"),     // 그룹별 공지
        GROUP_DISCUSSION("그룹게시판"), // 그룹별 자유게시판
        GROUP_RESOURCE("자료공유");    // 그룹별 자료공유

        private final String value;

        BoardType(String value) {
            this.value = value;
        }

    }

    @Builder
    public Board(String title, String content, User author, StudyGroup studyGroup, BoardType boardType) {
        this.title = Objects.requireNonNull(title, "제목은 필수입니다.");
        this.content = Objects.requireNonNull(content, "내용은 필수입니다.");
        this.author = Objects.requireNonNull(author, "작성자는 필수입니다.");
        this.boardType = Objects.requireNonNull(boardType, "게시판 유형은 필수입니다.");

        // 그룹 게시판인 경우 studyGroup은 필수
        if ((boardType == BoardType.GROUP_NOTICE || boardType == BoardType.GROUP_DISCUSSION || boardType == BoardType.GROUP_RESOURCE)
                && studyGroup == null) {
            throw new IllegalArgumentException("그룹 게시판에는 그룹 정보가 필요합니다.");
        }
        this.studyGroup = studyGroup;

        LocalDateTime now = LocalDateTime.now();
        this.postTime = now;
        this.editTime = now;
    }

    public void update(String title, String content) {
        this.title = Objects.requireNonNull(title, "제목은 필수입니다.");
        this.content = Objects.requireNonNull(content, "내용은 필수입니다.");
        this.editTime = LocalDateTime.now();
    }

    public boolean canAccess(User user) {
        if (studyGroup != null) {
            return studyGroup.isMember(user);
        }
        return true;
    }

    public boolean canModify(User user) {
        return author.equals(user) ||
                (studyGroup != null &&
                        (studyGroup.isLeader(user) || studyGroup.getMentor().equals(user)));
    }

    public boolean canWrite(User user) {
        return switch (boardType) {
            case NOTICE -> user.isMentor();
            case GROUP_NOTICE -> studyGroup != null &&
                    (studyGroup.isLeader(user) || user.equals(studyGroup.getMentor()));
            default -> true;
        };
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.postTime == null) {
            this.postTime = now;
        }
        if (this.editTime == null) {
            this.editTime = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.editTime = LocalDateTime.now();
    }

    public List<String> getContents() {
        // 전체 내용을 리스트로 반환하는 로직 (예: 내용 분리)
        return Arrays.asList(this.content.split("\n"));
    }

}
