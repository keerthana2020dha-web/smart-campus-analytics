package com.smartcampus.analytics.repository;

import com.smartcampus.analytics.model.StudentSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface StudentSessionRepository extends JpaRepository<StudentSession, Long> {
    Optional<StudentSession> findByTokenHashAndExpiresAtAfter(String tokenHash, Instant now);

    void deleteByTokenHash(String tokenHash);

    void deleteAllByExpiresAtBefore(Instant now);
}
