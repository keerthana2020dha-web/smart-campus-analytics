package com.smartcampus.analytics.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "faculty_student_feedback",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_faculty_student_feedback_author",
                columnNames = {"student_id", "faculty_id"}))
@Getter
@Setter
@NoArgsConstructor
public class FacultyStudentFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "student_id",
            referencedColumnName = "student_id",
            nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "faculty_id",
            referencedColumnName = "faculty_id",
            nullable = false)
    private FacultyAccount faculty;

    @Column(nullable = false)
    private Integer rating;

    @Column(name = "feedback_comment", length = 1000)
    private String comment;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
