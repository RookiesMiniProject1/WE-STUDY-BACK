package com.example.m_project1.controller;

import com.example.m_project1.dto.AuthDto;
import com.example.m_project1.entity.RefreshToken;
import com.example.m_project1.entity.User;
import com.example.m_project1.exception.InvalidOperationException;
import com.example.m_project1.repository.RefreshTokenRepository;
import com.example.m_project1.repository.UserRepository;
import com.example.m_project1.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthDto.RegisterRequest request) {
        try {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body("이미 존재하는 이메일입니다.");
            }

            User.Role role;
            try {
                role = User.Role.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("올바르지 않은 역할입니다. MENTOR 또는 MENTEE만 가능합니다.");
            }

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

            String encodedPassword = passwordEncoder.encode(request.getPassword());
            User user = new User(
                    request.getEmail(),
                    encodedPassword,
                    role,
                    career,
                    techStack
            );
            userRepository.save(user);

            HttpHeaders headers = getSecurityHeaders();
            return ResponseEntity.ok().headers(headers).body("회원가입이 완료되었습니다.");
        } catch (Exception e) {
            throw new InvalidOperationException("회원가입 처리 중 오류가 발생했습니다: " + e.getMessage());
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

            String roleAsString = user.getRole().name();
            String accessToken = jwtUtil.generateToken(user.getEmail(), roleAsString);
            String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

            // 기존 리프레시 토큰 삭제
            refreshTokenRepository.findByUserEmail(user.getEmail())
                    .ifPresent(refreshTokenRepository::delete);

            // 새 리프레시 토큰 저장
            refreshTokenRepository.save(new RefreshToken(
                    refreshToken,
                    user.getEmail(),
                    LocalDateTime.now().plusDays(7)
            ));

            HttpHeaders headers = getSecurityHeaders();
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new AuthDto.AuthResponse(
                            accessToken,
                            refreshToken,
                            user.getEmail(),
                            roleAsString
                    ));
        } catch (Exception e) {
            throw new InvalidOperationException(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                SecurityContextHolder.clearContext();
                HttpHeaders headers = getSecurityHeaders();
                return ResponseEntity.ok().headers(headers).body("로그아웃 되었습니다.");
            }
            return ResponseEntity.badRequest().body("잘못된 토큰 형식입니다.");
        } catch (Exception e) {
            throw new InvalidOperationException("로그아웃 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private HttpHeaders getSecurityHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Content-Type-Options", "nosniff");
        headers.add("X-Frame-Options", "DENY");
        headers.add("X-XSS-Protection", "1; mode=block");
        return headers;
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("리프레시 토큰이 존재하지 않습니다."));

        // 만료 여부 확인
        if (token.isExpired()) {
            throw new RuntimeException("리프레시 토큰이 만료되었습니다.");
        }

        // 새로운 액세스 토큰 발급
        String newAccessToken = jwtUtil.generateToken(token.getUserEmail(), "ROLE_USER");
        return ResponseEntity.ok(newAccessToken);
    }

}