package com.smartcampus.analytics.service;

import com.smartcampus.analytics.dto.FacultyLoginResponse;
import com.smartcampus.analytics.model.FacultyAccount;
import com.smartcampus.analytics.model.FacultySession;
import com.smartcampus.analytics.repository.FacultyAccountRepository;
import com.smartcampus.analytics.repository.FacultySessionRepository;
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
public class FacultyAuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);
    private static final Duration SESSION_DURATION = Duration.ofHours(8);

    private final FacultyAccountRepository accountRepository;
    private final FacultySessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final String dummyPasswordHash;
    private final SecureRandom secureRandom = new SecureRandom();

    public FacultyAuthService(FacultyAccountRepository accountRepository,
                              FacultySessionRepository sessionRepository,
                              PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.dummyPasswordHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public FacultyLoginResponse login(String facultyId, String password) {
        if (facultyId == null || facultyId.isBlank() || password == null || password.isBlank()) {
            throw invalidCredentials();
        }

        FacultyAccount account = accountRepository.findByFacultyId(facultyId.trim())
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
        FacultySession session = new FacultySession();
        session.setFacultyId(account.getFacultyId());
        session.setTokenHash(StudentSessionService.hash(token));
        session.setExpiresAt(expiresAt);
        sessionRepository.save(session);
        return new FacultyLoginResponse(token, expiresAt);
    }

    public void logout(String token) {
        if (token != null && !token.isBlank()) {
            sessionRepository.deleteByTokenHash(StudentSessionService.hash(token));
        }
    }

    private static ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid faculty credentials");
    }
}
