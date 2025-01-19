package com.example.m_project1.repository;

import com.example.m_project1.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    List<ChatRoom> findByStudyGroupId(Long groupId);
    Optional<ChatRoom> findByStudyGroupIdAndIsDefaultTrue(Long groupId);
    boolean existsByStudyGroupIdAndRoomName(Long groupId, String roomName);


}