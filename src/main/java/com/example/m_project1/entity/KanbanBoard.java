package com.example.m_project1.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Comparator;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "kanban_board")
public class KanbanBoard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private StudyGroup studyGroup;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "kanbanBoard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KanbanItem> items = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Builder
    public KanbanBoard(StudyGroup studyGroup, String title, String description) {
        this.studyGroup = Objects.requireNonNull(studyGroup, "스터디 그룹은 필수입니다.");
        this.title = Objects.requireNonNull(title, "제목은 필수입니다.");
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public void update(String title, String description) {
        this.title = Objects.requireNonNull(title, "제목은 필수입니다.");
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public void addItem(KanbanItem item) {
        items.add(item);
        item.setKanbanBoard(this);
    }

    public void removeItem(KanbanItem item) {
        items.remove(item);
        item.setKanbanBoard(null);
    }

    public List<KanbanItem> getItemsByStatus(KanbanItem.Status status) {
        return items.stream()
                .filter(item -> item.getStatus() == status)
                .sorted(Comparator.comparing(KanbanItem::getPriority))
                .collect(Collectors.toList());
    }

    public boolean canManage(User user) {
        return studyGroup.isLeader(user) || user.equals(studyGroup.getMentor());
    }
}