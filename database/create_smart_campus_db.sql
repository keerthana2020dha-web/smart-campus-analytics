CREATE DATABASE IF NOT EXISTS smart_campus_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE smart_campus_db;

CREATE TABLE IF NOT EXISTS students (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(100),
    semester INT,
    gpa DOUBLE,
    attendance_pct DOUBLE,
    subject_attendance_avg DOUBLE,
    lms_activity_hrs INT,
    placement_mock_score INT,
    internal_assessment_avg DOUBLE,
    backlogs INT,
    subject_performance_avg DOUBLE,
    lms_login_frequency INT,
    assignment_completion_pct DOUBLE,
    events_participated INT,
    clubs_participated INT,
    hackathons_participated INT,
    certifications INT,
    aptitude_score DOUBLE,
    coding_score DOUBLE,
    mock_interview_score DOUBLE,
    technical_skill_score DOUBLE,
    soft_skill_score DOUBLE,
    student_satisfaction_score DOUBLE,
    faculty_feedback_score DOUBLE,
    success_score DOUBLE,
    risk_category VARCHAR(30),
    academic_risk VARCHAR(30),
    placement_risk VARCHAR(30),
    academic_score DOUBLE,
    attendance_score DOUBLE,
    lms_score DOUBLE,
    engagement_score DOUBLE,
    placement_readiness_score DOUBLE,
    skills_score DOUBLE,
    feedback_score DOUBLE,
    data_coverage_pct DOUBLE,
    actionable_insight VARCHAR(1000),
    PRIMARY KEY (id),
    UNIQUE KEY uk_students_student_id (student_id),
    KEY idx_students_department (department),
    KEY idx_students_academic_risk (academic_risk),
    KEY idx_students_placement_risk (placement_risk)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS intervention_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id VARCHAR(50) NOT NULL,
    faculty_name VARCHAR(100),
    action_taken TEXT,
    status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_intervention_logs_student_id (student_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS course_grades (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id VARCHAR(50) NOT NULL,
    semester INT,
    course_name VARCHAR(120) NOT NULL,
    course_code VARCHAR(30),
    grade VARCHAR(30) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_course_grades_student_id (student_id),
    CONSTRAINT fk_course_grades_student
        FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS student_accounts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id VARCHAR(50) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    failed_attempts INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMP NULL,
    successful_login_count BIGINT NOT NULL DEFAULT 0,
    last_login_at TIMESTAMP NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_accounts_student_id (student_id),
    CONSTRAINT fk_student_accounts_student
        FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS student_sessions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id VARCHAR(50) NOT NULL,
    token_hash CHAR(64) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_sessions_token_hash (token_hash),
    KEY idx_student_sessions_student_id (student_id),
    CONSTRAINT fk_student_sessions_student
        FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS student_activities (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id VARCHAR(50) NOT NULL,
    activity_type VARCHAR(20) NOT NULL,
    activity_name VARCHAR(160) NOT NULL,
    activity_date DATE NULL,
    description VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_student_activities_student_id (student_id),
    CONSTRAINT fk_student_activities_student
        FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS student_certificates (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id VARCHAR(50) NOT NULL,
    certificate_title VARCHAR(160) NOT NULL,
    certificate_type VARCHAR(30) NOT NULL,
    issued_date DATE NULL,
    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(80) NOT NULL,
    sha256 CHAR(64) NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_certificate_checksum (student_id, sha256),
    KEY idx_student_certificates_student_id (student_id),
    CONSTRAINT fk_student_certificates_student
        FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS assignment_submissions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id VARCHAR(50) NOT NULL,
    semester INT NOT NULL,
    course_name VARCHAR(120) NOT NULL,
    course_code VARCHAR(30) NULL,
    assignment_title VARCHAR(160) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(80) NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_assignment_submissions_student_id (student_id),
    CONSTRAINT fk_assignment_submissions_student
        FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS faculty_accounts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    faculty_id VARCHAR(50) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    failed_attempts INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMP NULL,
    successful_login_count BIGINT NOT NULL DEFAULT 0,
    last_login_at TIMESTAMP NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_faculty_accounts_faculty_id (faculty_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS faculty_sessions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    faculty_id VARCHAR(50) NOT NULL,
    token_hash CHAR(64) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_faculty_sessions_token_hash (token_hash),
    KEY idx_faculty_sessions_faculty_id (faculty_id),
    CONSTRAINT fk_faculty_sessions_account
        FOREIGN KEY (faculty_id) REFERENCES faculty_accounts(faculty_id) ON DELETE CASCADE
) ENGINE=InnoDB;
