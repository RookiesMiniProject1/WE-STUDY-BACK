package com.example.m_project1.Service;

import com.example.m_project1.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@EnableScheduling
@RequiredArgsConstructor
public class TokenSchedulerService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(fixedRate = 86400000)
    public void removeExpiredTokens() {
        refreshTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }
}