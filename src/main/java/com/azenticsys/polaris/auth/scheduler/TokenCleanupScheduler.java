package com.azenticsys.polaris.auth.scheduler;

import com.azenticsys.polaris.auth.repository.RevokedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final RevokedTokenRepository revokedTokenRepository;

    // Corre cada día a medianoche
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        revokedTokenRepository.deleteExpired(LocalDateTime.now());
    }
}
