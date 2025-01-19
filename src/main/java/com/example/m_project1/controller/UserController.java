package com.example.m_project1.controller;

import com.example.m_project1.dto.ChangePasswordRequest;
import com.example.m_project1.dto.CommonDto;
import com.example.m_project1.dto.UpdateProfileRequest;
import com.example.m_project1.dto.UserProfileResponse;
import com.example.m_project1.entity.User;
import com.example.m_project1.repository.UserRepository;
import com.example.m_project1.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;



    // 1. 프로필 조회
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Authentication is null. Please check your JWT token.");
            }

            String email = authentication.getName(); // 이메일 추출
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            UserProfileResponse response;
            if (user.getRole() == User.Role.MENTOR) {
                response = UserProfileResponse.forMentor(
                        user.getUserId(),
                        user.getEmail(),
                        user.getRole().name(),
                        user.getCareer(),
                        user.getTechStack()
                );
            } else {
                response = UserProfileResponse.forMentee(
                        user.getUserId(),
                        user.getEmail(),
                        user.getRole().name()
                );
            }

            return ResponseEntity.ok(CommonDto.ApiResponse.success("프로필 조회 성공", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("프로필 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }


    // 2. 비밀번호 변경
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName(); // 이메일 추출
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(CommonDto.ApiResponse.error("기존 비밀번호가 올바르지 않습니다.", HttpStatus.BAD_REQUEST));
            }

            if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(CommonDto.ApiResponse.error("새 비밀번호는 기존 비밀번호와 동일할 수 없습니다.", HttpStatus.BAD_REQUEST));
            }

            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            return ResponseEntity.ok(CommonDto.ApiResponse.success("비밀번호가 성공적으로 변경되었습니다.", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonDto.ApiResponse.error("비밀번호 변경 중 오류가 발생했습니다: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    // 3. 사용자 정보 수정
    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            // 이메일 수정 로직
            if (request.getEmail() != null && !user.getEmail().equals(request.getEmail())) {
                if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                    return ResponseEntity.badRequest().body("이미 존재하는 이메일입니다.");
                }
                user.setEmail(request.getEmail());
            }


            // 관심 기술 저장
            if (request.getInterestSkills() != null) {
                user.setInterestSkills(request.getInterestSkills());
            }
            // 경력 및 기술 스택 수정
            if (user.getRole() == User.Role.MENTOR) {
                user.setCareer(request.getCareer());
                user.setTechStack(request.getTechStack());
            } else if (user.getRole() == User.Role.MENTEE) {
                if (request.getCareer() != null || request.getTechStack() != null) {
                    return ResponseEntity.badRequest().body("멘티는 경력과 기술 스택을 수정할 수 없습니다.");
                }
            }


            userRepository.save(user);

            // 새 토큰 생성
            String newToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
            return ResponseEntity.ok(Map.of(
                    "message", "프로필이 성공적으로 업데이트되었습니다.",
                    "newToken", newToken
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("프로필 업데이트 중 오류가 발생했습니다: " + e.getMessage());
        }
    }


}
