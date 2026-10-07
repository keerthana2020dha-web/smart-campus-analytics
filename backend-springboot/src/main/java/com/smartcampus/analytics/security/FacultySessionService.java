package com.smartcampus.analytics.security;

import com.smartcampus.analytics.model.FacultySession;
import com.smartcampus.analytics.repository.FacultySessionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class FacultySessionService {

    private final FacultySessionRepository sessionRepository;

    public FacultySessionService(FacultySessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public Optional<String> facultyIdForToken(String token) {
        return sessionRepository.findByTokenHashAndExpiresAtAfter(
                        StudentSessionService.hash(token), Instant.now())
                .map(FacultySession::getFacultyId);
    }
}
