CREATE TABLE IF NOT EXISTS student_staff_feedback (
    id BIGINT NOT NULL AUTO_INCREMENT,
    staff_name VARCHAR(120) NOT NULL,
    rating TINYINT NOT NULL,
    feedback_comment VARCHAR(1000) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_student_staff_feedback_staff_name (staff_name),
    CONSTRAINT chk_student_staff_feedback_rating CHECK (rating BETWEEN 1 AND 5)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS faculty_student_feedback (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id VARCHAR(50) NOT NULL,
    faculty_id VARCHAR(50) NOT NULL,
    rating TINYINT NOT NULL,
    feedback_comment VARCHAR(1000) NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_faculty_student_feedback_author (student_id, faculty_id),
    KEY idx_faculty_student_feedback_student (student_id),
    CONSTRAINT fk_faculty_student_feedback_student
        FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    CONSTRAINT fk_faculty_student_feedback_faculty
        FOREIGN KEY (faculty_id) REFERENCES faculty_accounts(faculty_id) ON DELETE CASCADE,
    CONSTRAINT chk_faculty_student_feedback_rating CHECK (rating BETWEEN 1 AND 5)
) ENGINE=InnoDB;
