package com.smartcampus.analytics.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "student_staff_feedback")
@Getter
@Setter
@NoArgsConstructor
public class StudentStaffFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "staff_name", nullable = false, length = 120)
    private String staffName;

    @Column(nullable = false)
    private Integer rating;

    @Column(name = "feedback_comment", length = 1000)
    private String comment;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
