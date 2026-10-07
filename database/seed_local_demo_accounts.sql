-- LOCAL TESTING ONLY. These public demo credentials are not for production.
-- Student login password is the demo DOB: 2004-01-15.
START TRANSACTION;

INSERT INTO students (
    student_id, name, department, semester, gpa, attendance_pct,
    subject_attendance_avg, lms_activity_hrs, placement_mock_score,
    internal_assessment_avg, backlogs, subject_performance_avg,
    lms_login_frequency, assignment_completion_pct, events_participated,
    clubs_participated, hackathons_participated, certifications,
    aptitude_score, coding_score, mock_interview_score, technical_skill_score,
    soft_skill_score, student_satisfaction_score, faculty_feedback_score,
    success_score, risk_category, academic_risk, placement_risk,
    academic_score, attendance_score, lms_score, engagement_score,
    placement_readiness_score, skills_score, feedback_score, data_coverage_pct,
    actionable_insight
) VALUES (
    'DEMO-STU-001', 'Demo Student', 'IT', 3, 7.8, 85.0,
    82.0, 12, 70, 76.0, 0, 78.0,
    18, 80.0, 2, 1, 1, 1,
    72.0, 74.0, 68.0, 75.0, 70.0, 80.0, 82.0,
    76.0, 'Low', 'Low', 'Moderate',
    78.0, 85.0, 75.0, 70.0, 71.0, 73.0, 81.0, 95.0,
    'Local demo account for testing; do not use as a real student record.'
) ON DUPLICATE KEY UPDATE student_id = student_id;

INSERT INTO faculty_accounts (
    faculty_id, password_hash, failed_attempts, successful_login_count
) VALUES (
    'DEMO-FAC-001',
    '$2a$12$zVQQyzM2kI820dbAnuA22u.PqIEmhibUrfmFTAB7EDw00GoH8/rSK',
    0, 0
) ON DUPLICATE KEY UPDATE
    password_hash = VALUES(password_hash),
    failed_attempts = 0,
    locked_until = NULL;

INSERT INTO student_accounts (
    student_id, password_hash, failed_attempts, successful_login_count
) VALUES (
    'DEMO-STU-001',
    '$2a$12$DqbMoOiIHeiRd8XFuaLFyOfA6EKifQR.3sDfJ6FP0fg8zQ.c8JAKG',
    0, 0
) ON DUPLICATE KEY UPDATE
    password_hash = VALUES(password_hash),
    failed_attempts = 0,
    locked_until = NULL;

COMMIT;
