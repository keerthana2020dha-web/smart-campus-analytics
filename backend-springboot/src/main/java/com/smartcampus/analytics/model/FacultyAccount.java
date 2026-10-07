package com.smartcampus.analytics.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "faculty_accounts")
@Getter
@Setter
@NoArgsConstructor
public class FacultyAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "faculty_id", nullable = false, unique = true, length = 50)
    private String facultyId;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "successful_login_count", nullable = false)
    private long successfulLoginCount;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;
}
