CREATE TABLE IF NOT EXISTS course_grades (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(50) NOT NULL,
    course_name VARCHAR(120) NOT NULL,
    course_code VARCHAR(30),
    grade VARCHAR(30) NOT NULL,
    CONSTRAINT fk_course_grades_student
        FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    INDEX idx_course_grades_student_id (student_id)
);
