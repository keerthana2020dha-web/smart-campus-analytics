package com.smartcampus.analytics.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@Table(
        name = "student_portal_activity_sessions",
        indexes = {
                @Index(name = "idx_student_activity_student_started",
                        columnList = "student_id, started_at"),
                @Index(name = "idx_student_activity_started", columnList = "started_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class StudentPortalActivitySession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Student student;

    @Column(name = "token_hash", nullable = false, length = 64, unique = true)
    private String tokenHash;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "last_heartbeat_at", nullable = false)
    private Instant lastHeartbeatAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "active_seconds", nullable = false)
    private long activeSeconds;
}
