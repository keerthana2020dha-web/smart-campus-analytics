package com.smartcampus.analytics.repository;

import com.smartcampus.analytics.model.StudentPortalActivitySession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface StudentPortalActivitySessionRepository
        extends JpaRepository<StudentPortalActivitySession, Long> {

    Optional<StudentPortalActivitySession> findByTokenHash(String tokenHash);

    List<StudentPortalActivitySession> findAllByStartedAtAfter(Instant since);
}
