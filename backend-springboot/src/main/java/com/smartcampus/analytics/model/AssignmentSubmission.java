package com.smartcampus.analytics.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "assignment_submissions", indexes = @Index(
        name = "idx_assignment_submissions_student_id", columnList = "student_id"))
@Getter
@Setter
@NoArgsConstructor
public class AssignmentSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private Integer semester;

    @Column(name = "course_name", nullable = false, length = 120)
    private String courseName;

    @Column(name = "course_code", length = 30)
    private String courseCode;

    @Column(name = "assignment_title", nullable = false, length = 160)
    private String assignmentTitle;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "stored_file_name", nullable = false, length = 80)
    private String storedFileName;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt = Instant.now();
}
