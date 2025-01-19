package com.example.m_project1.repository;

import com.example.m_project1.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email); // 이메일로 사용자
    // 전체 사용자 조회 (멘토/멘티 구분 없이)
    @Query("SELECT DISTINCT u FROM User u")
    List<User> findAllUsers();

    // 역할별로 조회,혹시모르니까
    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findByRole(@Param("role") User.Role role);
 }
