package com.smartcampus.analytics.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false, unique = true)
    private String studentId;

    @Column(nullable = false)
    private String name;

    private String department;
    private Integer semester;

    @Column(name = "gpa")
    private Double gpa;

    @Column(name = "attendance_pct")
    private Double attendancePct;

    @Column(name = "subject_attendance_avg")
    private Double subjectAttendanceAvg;

    @Column(name = "lms_activity_hrs")
    private Integer lmsActivityHrs;

    @Column(name = "placement_mock_score")
    private Integer placementMockScore;

    @Column(name = "internal_assessment_avg")
    private Double internalAssessmentAvg;

    private Integer backlogs;

    @Column(name = "subject_performance_avg")
    private Double subjectPerformanceAvg;

    @Column(name = "lms_login_frequency")
    private Integer lmsLoginFrequency;

    @Column(name = "assignment_completion_pct")
    private Double assignmentCompletionPct;

    private Integer eventsParticipated;
    private Integer clubsParticipated;
    private Integer hackathonsParticipated;
    private Integer certifications;

    @Column(name = "aptitude_score")
    private Double aptitudeScore;

    @Column(name = "coding_score")
    private Double codingScore;

    @Column(name = "mock_interview_score")
    private Double mockInterviewScore;

    @Column(name = "technical_skill_score")
    private Double technicalSkillScore;

    @Column(name = "soft_skill_score")
    private Double softSkillScore;

    @Column(name = "student_satisfaction_score")
    private Double studentSatisfactionScore;

    @Column(name = "faculty_feedback_score")
    private Double facultyFeedbackScore;

    @Column(name = "success_score")
    private Double successScore;

    @Column(name = "risk_category")
    private String riskCategory;

    @Column(name = "academic_risk")
    private String academicRisk;

    @Column(name = "placement_risk")
    private String placementRisk;

    @Column(name = "academic_score")
    private Double academicScore;

    @Column(name = "attendance_score")
    private Double attendanceScore;

    @Column(name = "lms_score")
    private Double lmsScore;

    @Column(name = "engagement_score")
    private Double engagementScore;

    @Column(name = "placement_readiness_score")
    private Double placementReadinessScore;

    @Column(name = "skills_score")
    private Double skillsScore;

    @Column(name = "feedback_score")
    private Double feedbackScore;

    @Column(name = "data_coverage_pct")
    private Double dataCoveragePct;

    @Column(name = "actionable_insight", length = 1000)
    private String actionableInsight;

    @OneToMany(mappedBy = "student", cascade = CascadeType.REMOVE)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<CourseGrade> courseGrades = new ArrayList<>();
}