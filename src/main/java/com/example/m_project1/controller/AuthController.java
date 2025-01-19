package com.example.m_project1.controller;

import com.example.m_project1.dto.AuthDto;
import com.example.m_project1.dto.CommonDto;
import com.example.m_project1.entity.User;
import com.example.m_project1.repository.UserRepository;
import com.example.m_project1.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthDto.RegisterRequest request) {
        try {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body("이미 존재하는 이메일입니다.");
            }

            // Role 검증 및 Enum 변환
            User.Role role;
            try {
                role = User.Role.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("올바르지 않은 역할입니다. MENTOR 또는 MENTEE만 가능합니다.");
            }

            // MENTOR 또는 MENTEE에 따른 추가 필드 처리
            String career = null;
            String techStack = null;
            if (role == User.Role.MENTOR) {
                if (request.getCareer() == null || request.getTechStack() == null) {
                    return ResponseEntity.badRequest().body("멘토는 경력 및 주요 기술 스택을 입력해야 합니다.");
                }
                career = request.getCareer();
                techStack = request.getTechStack();
            } else if (role == User.Role.MENTEE) {
                if (request.getCareer() != null || request.getTechStack() != null) {
                    return ResponseEntity.badRequest().body("멘티는 경력 및 기술 스택을 입력할 수 없습니다.");
                }
            }

            // 사용자 저장
            String encodedPassword = passwordEncoder.encode(request.getPassword());
            User user = new User(
                    request.getEmail(),
                    encodedPassword,
                    role,
                    career,
                    techStack
            );
            userRepository.save(user);

            return ResponseEntity.ok("회원가입이 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("회원가입 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthDto.LoginRequest request) {
        try {
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new RuntimeException("비밀번호가 올바르지 않습니다.");
            }

            // Role을 String으로 변환
            String roleAsString = user.getRole().name();

            String accessToken = jwtUtil.generateToken(user.getEmail(), roleAsString);

            return ResponseEntity.ok(new AuthDto.AuthResponse(
                    accessToken,
                    user.getEmail(),
                    roleAsString
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                SecurityContextHolder.clearContext();
                return ResponseEntity.ok("로그아웃 되었습니다.");
            }
            return ResponseEntity.badRequest().body("잘못된 토큰 형식입니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("로그아웃 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }


}
