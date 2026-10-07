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
