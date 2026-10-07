package com.smartcampus.analytics.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "course_grades")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CourseGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "semester")
    private Integer semester;

    @Column(name = "course_name", nullable = false, length = 120)
    private String courseName;

    @Column(name = "course_code", length = 30)
    private String courseCode;

    @Column(nullable = false, length = 30)
    private String grade;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private Student student;
}
