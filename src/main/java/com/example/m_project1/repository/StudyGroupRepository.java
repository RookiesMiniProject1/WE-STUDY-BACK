package com.example.m_project1.repository;

import com.example.m_project1.entity.StudyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {
    boolean existsByTitle(String title); // 그룹명 중복 확인
    // 사용자 ID로 그룹 조회
    // 오류발생
    //그냥 쿼리넣기
    @Query("SELECT sg FROM StudyGroup sg JOIN sg.members usg WHERE usg.user.userId = :userId")
    List<StudyGroup> findByUserId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT sg FROM StudyGroup sg " +
            "LEFT JOIN sg.members m " +
            "LEFT JOIN sg.joinRequests r " +
            "WHERE (m.user.userId = :userId) OR (r.user.userId = :userId)")
    List<StudyGroup> findUserRelatedGroups(@Param("userId") Long userId);
}
