package com.example.m_project1.repository;

import com.example.m_project1.entity.StudyGroup;
import com.example.m_project1.entity.UserStudyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserStudyGroupRepository extends JpaRepository<UserStudyGroup, Long> {
    List<UserStudyGroup> findByStudyGroupId(Long studyGroupId);

    List<UserStudyGroup> findByUser_UserId(Long userId);


    Optional<UserStudyGroup> findByStudyGroup_IdAndUser_UserId(Long studyGroupId, Long userId);


    List<UserStudyGroup> findByStudyGroupIdAndStatus(Long groupId, StudyGroup.JoinStatus status);

    boolean existsByStudyGroup_IdAndUser_UserIdAndStatus(Long studyGroupId, Long userId, StudyGroup.JoinStatus status);

    List<UserStudyGroup> findByUser_UserIdAndStatus(Long userId, StudyGroup.JoinStatus status);


    void deleteByStudyGroupId(Long studyGroupId);
}
