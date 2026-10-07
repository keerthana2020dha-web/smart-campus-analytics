package com.smartcampus.analytics.service;

import com.smartcampus.analytics.dto.StudentLoginResponse;
import com.smartcampus.analytics.model.StudentAccount;
import com.smartcampus.analytics.model.StudentSession;
import com.smartcampus.analytics.repository.StudentAccountRepository;
import com.smartcampus.analytics.repository.StudentSessionRepository;
import com.smartcampus.analytics.security.StudentSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class StudentAuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);
    private static final Duration SESSION_DURATION = Duration.ofHours(8);

    private final StudentAccountRepository accountRepository;
    private final StudentSessionRepository sessionRepository;
    private final StudentPortalActivityService activityService;
    private final PasswordEncoder passwordEncoder;
    private final String dummyPasswordHash;
    private final SecureRandom secureRandom = new SecureRandom();

    public StudentAuthService(StudentAccountRepository accountRepository,
                              StudentSessionRepository sessionRepository,
                              StudentPortalActivityService activityService,
                              PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.sessionRepository = sessionRepository;
        this.activityService = activityService;
        this.passwordEncoder = passwordEncoder;
        this.dummyPasswordHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public StudentLoginResponse login(String studentId, String password) {
        if (studentId == null || studentId.isBlank() || password == null || password.isBlank()) {
            throw invalidCredentials();
        }

        StudentAccount account = accountRepository.findByStudentStudentId(studentId.trim())
                .orElse(null);
        if (account == null) {
            passwordEncoder.matches(password, dummyPasswordHash);
            throw invalidCredentials();
        }

        Instant now = Instant.now();
        if (account.getLockedUntil() != null && account.getLockedUntil().isAfter(now)) {
            passwordEncoder.matches(password, account.getPasswordHash());
            throw invalidCredentials();
        }

        if (!passwordEncoder.matches(password, account.getPasswordHash())) {
            int failures = account.getFailedAttempts() + 1;
            account.setFailedAttempts(failures >= MAX_FAILED_ATTEMPTS ? 0 : failures);
            account.setLockedUntil(
                    failures >= MAX_FAILED_ATTEMPTS ? now.plus(LOCK_DURATION) : null);
            accountRepository.save(account);
            throw invalidCredentials();
        }

        account.setFailedAttempts(0);
        account.setLockedUntil(null);
        account.setSuccessfulLoginCount(account.getSuccessfulLoginCount() + 1);
        account.setLastLoginAt(now);
        accountRepository.save(account);

        byte[] randomToken = new byte[32];
        secureRandom.nextBytes(randomToken);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomToken);
        Instant expiresAt = now.plus(SESSION_DURATION);
        sessionRepository.deleteAllByExpiresAtBefore(now);
        StudentSession session = new StudentSession();
        session.setStudentId(account.getStudent().getStudentId());
        session.setTokenHash(StudentSessionService.hash(token));
        session.setExpiresAt(expiresAt);
        sessionRepository.save(session);
        activityService.startSession(account.getStudent(), token, now);
        return new StudentLoginResponse(token, expiresAt);
    }

    public void logout(String token) {
        if (token != null && !token.isBlank()) {
            activityService.endSession(token);
            sessionRepository.deleteByTokenHash(StudentSessionService.hash(token));
        }
    }

    private static ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Invalid registration number or password");
    }
}
