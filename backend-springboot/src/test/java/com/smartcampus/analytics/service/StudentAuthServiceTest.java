package com.smartcampus.analytics.service;

import com.smartcampus.analytics.model.Student;
import com.smartcampus.analytics.model.StudentAccount;
import com.smartcampus.analytics.model.StudentSession;
import com.smartcampus.analytics.repository.StudentAccountRepository;
import com.smartcampus.analytics.repository.StudentSessionRepository;
import com.smartcampus.analytics.security.StudentSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StudentAuthServiceTest {

    private StudentAccountRepository accountRepository;
    private StudentSessionRepository sessionRepository;
    private StudentPortalActivityService activityService;
    private StudentAuthService authService;
    private Student student;
    private StudentAccount account;

    @BeforeEach
    void setUp() {
        accountRepository = mock(StudentAccountRepository.class);
        sessionRepository = mock(StudentSessionRepository.class);
        activityService = mock(StudentPortalActivityService.class);
        authService = new StudentAuthService(
                accountRepository,
                sessionRepository,
                activityService,
                new BCryptPasswordEncoder(4));

        student = new Student();
        student.setStudentId("STU-101");
        account = new StudentAccount();
        account.setStudent(student);
        account.setPasswordHash(new BCryptPasswordEncoder(4).encode("2005-04-12"));
        when(accountRepository.findByStudentStudentId("STU-101"))
                .thenReturn(Optional.of(account));
    }

    @Test
    void successfulLoginCreatesHashedSessionAndIncrementsCount() {
        var response = authService.login(" STU-101 ", "2005-04-12");

        assertEquals(43, response.accessToken().length());
        assertTrue(response.expiresAt().isAfter(Instant.now().plus(Duration.ofHours(7))));
        assertEquals(1, account.getSuccessfulLoginCount());
        assertNotNull(account.getLastLoginAt());
        verify(sessionRepository).deleteAllByExpiresAtBefore(any(Instant.class));
        var sessionCaptor = org.mockito.ArgumentCaptor.forClass(StudentSession.class);
        verify(sessionRepository).save(sessionCaptor.capture());
        assertEquals("STU-101", sessionCaptor.getValue().getStudentId());
        assertNotEquals(response.accessToken(),
                sessionCaptor.getValue().getTokenHash());
        assertEquals(StudentSessionService.hash(response.accessToken()),
                sessionCaptor.getValue().getTokenHash());
        verify(activityService).startSession(
                eq(student), eq(response.accessToken()), any(Instant.class));
    }

    @Test
    void failedLoginsRecordAttemptsAndSetLockoutOnFifthFailure() {
        for (int attempt = 1; attempt <= 5; attempt++) {
            assertThrows(ResponseStatusException.class,
                    () -> authService.login("STU-101", "wrong-password"));
        }

        assertEquals(0, account.getFailedAttempts());
        assertNotNull(account.getLockedUntil());
        assertTrue(account.getLockedUntil().isAfter(Instant.now()));
        verify(accountRepository, times(5)).save(account);
        assertThrows(ResponseStatusException.class,
                () -> authService.login("STU-101", "2005-04-12"));
        verify(accountRepository, times(5)).save(account);
    }

    @Test
    void failedAttemptUpdatesAreConfiguredToCommitDespiteUnauthorizedResponse() throws Exception {
        var transactional = StudentAuthService.class
                .getMethod("login", String.class, String.class)
                .getAnnotation(Transactional.class);

        assertNotNull(transactional);
        assertArrayEquals(new Class<?>[]{ResponseStatusException.class},
                transactional.noRollbackFor());
    }
}
