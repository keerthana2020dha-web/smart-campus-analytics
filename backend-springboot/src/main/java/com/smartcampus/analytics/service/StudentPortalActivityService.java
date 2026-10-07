package com.smartcampus.analytics.service;

import com.smartcampus.analytics.dto.WeeklyStudentActivityDTO;
import com.smartcampus.analytics.model.Student;
import com.smartcampus.analytics.model.StudentPortalActivitySession;
import com.smartcampus.analytics.repository.StudentPortalActivitySessionRepository;
import com.smartcampus.analytics.repository.StudentSessionRepository;
import com.smartcampus.analytics.security.StudentSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StudentPortalActivityService {

    private static final Duration WEEK_WINDOW = Duration.ofDays(7);
    private static final Duration MAX_HEARTBEAT_GAP = Duration.ofSeconds(90);

    private final StudentPortalActivitySessionRepository activityRepository;
    private final StudentSessionRepository sessionRepository;

    public StudentPortalActivityService(
            StudentPortalActivitySessionRepository activityRepository,
            StudentSessionRepository sessionRepository) {
        this.activityRepository = activityRepository;
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public void startSession(Student student, String token, Instant startedAt) {
        StudentPortalActivitySession activity = new StudentPortalActivitySession();
        activity.setStudent(student);
        activity.setTokenHash(StudentSessionService.hash(token));
        activity.setStartedAt(startedAt);
        activity.setLastHeartbeatAt(startedAt);
        activity.setActiveSeconds(0);
        activityRepository.save(activity);
    }

    @Transactional
    public void heartbeat(String token, boolean active) {
        Instant now = Instant.now();
        String tokenHash = StudentSessionService.hash(token);
        if (sessionRepository.findByTokenHashAndExpiresAtAfter(tokenHash, now).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Student session expired");
        }

        StudentPortalActivitySession activity = activityRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Student activity session not found"));
        if (activity.getEndedAt() != null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Student session ended");
        }

        long elapsedSeconds = Duration.between(activity.getLastHeartbeatAt(), now).toSeconds();
        if (active && elapsedSeconds > 0
                && elapsedSeconds <= MAX_HEARTBEAT_GAP.toSeconds()) {
            activity.setActiveSeconds(activity.getActiveSeconds() + elapsedSeconds);
        }
        activity.setLastHeartbeatAt(now);
    }

    @Transactional
    public void endSession(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        activityRepository.findByTokenHash(StudentSessionService.hash(token))
                .ifPresent(activity -> {
                    if (activity.getEndedAt() == null) {
                        activity.setEndedAt(Instant.now());
                    }
                });
    }

    @Transactional(readOnly = true)
    public List<WeeklyStudentActivityDTO> getWeeklyActivity() {
        Instant since = Instant.now().minus(WEEK_WINDOW);
        Map<String, long[]> totalsByStudent = new LinkedHashMap<>();
        for (StudentPortalActivitySession activity :
                activityRepository.findAllByStartedAtAfter(since)) {
            String studentId = activity.getStudent().getStudentId();
            long[] totals = totalsByStudent.computeIfAbsent(studentId, ignored -> new long[2]);
            totals[0]++;
            totals[1] += activity.getActiveSeconds();
        }

        List<WeeklyStudentActivityDTO> results = new ArrayList<>();
        totalsByStudent.forEach((studentId, totals) -> results.add(
                new WeeklyStudentActivityDTO(
                        studentId,
                        totals[0],
                        totals[1],
                        totals[1] / 3600.0
                )
        ));
        return results;
    }
}
