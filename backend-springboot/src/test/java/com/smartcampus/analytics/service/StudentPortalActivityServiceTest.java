package com.smartcampus.analytics.service;

import com.smartcampus.analytics.model.StudentPortalActivitySession;
import com.smartcampus.analytics.model.StudentSession;
import com.smartcampus.analytics.repository.StudentPortalActivitySessionRepository;
import com.smartcampus.analytics.repository.StudentSessionRepository;
import com.smartcampus.analytics.security.StudentSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StudentPortalActivityServiceTest {

    private static final String TOKEN =
            "test-student-session-token-with-a-long-enough-value";

    private StudentPortalActivitySessionRepository activityRepository;
    private StudentSessionRepository sessionRepository;
    private StudentPortalActivityService activityService;
    private StudentPortalActivitySession activity;

    @BeforeEach
    void setUp() {
        activityRepository = mock(StudentPortalActivitySessionRepository.class);
        sessionRepository = mock(StudentSessionRepository.class);
        activityService = new StudentPortalActivityService(
                activityRepository, sessionRepository);

        StudentSession session = new StudentSession();
        session.setExpiresAt(Instant.now().plusSeconds(3600));
        when(sessionRepository.findByTokenHashAndExpiresAtAfter(
                org.mockito.ArgumentMatchers.eq(StudentSessionService.hash(TOKEN)),
                org.mockito.ArgumentMatchers.any(Instant.class)))
                .thenReturn(Optional.of(session));

        activity = new StudentPortalActivitySession();
        activity.setLastHeartbeatAt(Instant.now().minusSeconds(30));
        activity.setActiveSeconds(12);
        when(activityRepository.findByTokenHash(StudentSessionService.hash(TOKEN)))
                .thenReturn(Optional.of(activity));
    }

    @Test
    void activeHeartbeatAddsRecentTimeToSession() {
        activityService.heartbeat(TOKEN, true);

        assertEquals(42, activity.getActiveSeconds());
    }

    @Test
    void inactiveHeartbeatDoesNotAddTime() {
        activityService.heartbeat(TOKEN, false);

        assertEquals(12, activity.getActiveSeconds());
    }

    @Test
    void delayedHeartbeatDoesNotCountUnobservedTime() {
        activity.setLastHeartbeatAt(Instant.now().minusSeconds(120));

        activityService.heartbeat(TOKEN, true);

        assertEquals(12, activity.getActiveSeconds());
    }
}
