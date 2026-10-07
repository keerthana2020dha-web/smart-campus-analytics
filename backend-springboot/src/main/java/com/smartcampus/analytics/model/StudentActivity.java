package com.smartcampus.analytics.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.Instant;

@Entity
@Table(name = "student_activities", indexes = @Index(
        name = "idx_student_activities_student_id", columnList = "student_id"))
@Getter
@Setter
@NoArgsConstructor
public class StudentActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id", nullable = false)
    private Student student;

    @Column(name = "activity_type", nullable = false, length = 20)
    private String activityType;

    @Column(name = "activity_name", nullable = false, length = 160)
    private String activityName;

    @Column(name = "activity_date")
    private LocalDate activityDate;

    @Column(length = 500)
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
