package com.smartcampus.analytics.service;

import com.smartcampus.analytics.model.FacultyAccount;
import com.smartcampus.analytics.model.FacultySession;
import com.smartcampus.analytics.repository.FacultyAccountRepository;
import com.smartcampus.analytics.repository.FacultySessionRepository;
import com.smartcampus.analytics.security.StudentSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FacultyAuthServiceTest {

    private FacultyAccountRepository accountRepository;
    private FacultySessionRepository sessionRepository;
    private FacultyAuthService authService;
    private FacultyAccount account;

    @BeforeEach
    void setUp() {
        accountRepository = mock(FacultyAccountRepository.class);
        sessionRepository = mock(FacultySessionRepository.class);
        var encoder = new BCryptPasswordEncoder(4);
        authService = new FacultyAuthService(accountRepository, sessionRepository, encoder);

        account = new FacultyAccount();
        account.setFacultyId("FAC-101");
        account.setPasswordHash(encoder.encode("strong-faculty-password"));
        when(accountRepository.findByFacultyId("FAC-101")).thenReturn(Optional.of(account));
    }

    @Test
    void successfulLoginCreatesHashedSessionAndIncrementsCount() {
        var response = authService.login(" FAC-101 ", "strong-faculty-password");

        assertEquals(43, response.accessToken().length());
        assertTrue(response.expiresAt().isAfter(Instant.now().plus(Duration.ofHours(7))));
        assertEquals(1, account.getSuccessfulLoginCount());
        var sessionCaptor = org.mockito.ArgumentCaptor.forClass(FacultySession.class);
        verify(sessionRepository).save(sessionCaptor.capture());
        assertEquals("FAC-101", sessionCaptor.getValue().getFacultyId());
        assertEquals(StudentSessionService.hash(response.accessToken()),
                sessionCaptor.getValue().getTokenHash());
        verify(sessionRepository).deleteAllByExpiresAtBefore(any(Instant.class));
    }

    @Test
    void fifthFailedLoginLocksFacultyAccount() {
        for (int attempt = 1; attempt <= 5; attempt++) {
            assertThrows(ResponseStatusException.class,
                    () -> authService.login("FAC-101", "wrong-password"));
        }

        assertEquals(0, account.getFailedAttempts());
        assertNotNull(account.getLockedUntil());
        assertTrue(account.getLockedUntil().isAfter(Instant.now()));
        verify(accountRepository, times(5)).save(account);
    }
}
