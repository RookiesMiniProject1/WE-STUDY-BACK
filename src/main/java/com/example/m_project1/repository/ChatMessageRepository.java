package com.example.m_project1.repository;

import com.example.m_project1.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoomIdOrderByTimestampAsc(Long roomId);
    List<ChatMessage> findByChatRoomIdOrderByTimestampDesc(Long roomId, Pageable pageable);


    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom.id = :roomId " +
            "AND m.timestamp >= :since ORDER BY m.timestamp ASC")
    List<ChatMessage> findRecentMessages(
            @Param("roomId") Long roomId,
            @Param("since") LocalDateTime since
    );

    void deleteByChatRoomId(Long roomId);


}
