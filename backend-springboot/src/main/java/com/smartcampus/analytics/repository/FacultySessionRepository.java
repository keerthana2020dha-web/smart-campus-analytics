package com.smartcampus.analytics.repository;

import com.smartcampus.analytics.model.FacultySession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface FacultySessionRepository extends JpaRepository<FacultySession, Long> {
    Optional<FacultySession> findByTokenHashAndExpiresAtAfter(String tokenHash, Instant now);

    void deleteByTokenHash(String tokenHash);

    void deleteAllByExpiresAtBefore(Instant now);
}
