package com.example.m_project1.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(length = 50, nullable = false, unique = true)
    private String email;

    @Column(length = 100, nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    @Builder.Default
    private Role role = Role.MENTEE;

    @Column(length = 200)
    private String career;

    @Column(length = 500)
    private String techStack;

    @Column(length = 500)
    private String interestSkills;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserStudyGroup> studyGroups = new ArrayList<>();

    public enum Role {
        MENTOR, MENTEE
    }

    public User(String email, String password, Role role, String career, String techStack) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.career = career;
        this.techStack = techStack;
    }

    public void addInterestSkill(String skill) {
        if (this.interestSkills == null) {
            this.interestSkills = skill;
        } else {
            this.interestSkills = this.interestSkills + "," + skill;
        }
    }

    public void removeInterestSkill(String skill) {
        if (this.interestSkills != null) {
            List<String> skills = new ArrayList<>(Arrays.asList(this.interestSkills.split(",")));
            skills.remove(skill);
            this.interestSkills = String.join(",", skills);
        }
    }

    public String[] getInterestSkillsAsArray() {
        return interestSkills != null ? interestSkills.split(",") : new String[0];
    }

    public boolean isMentor() {
        return this.role == Role.MENTOR;
    }
}
