package com.smartcampus.analytics.repository;

import com.smartcampus.analytics.model.StudentStaffFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentStaffFeedbackRepository extends JpaRepository<StudentStaffFeedback, Long> {
    List<StudentStaffFeedback> findAllByOrderByCreatedAtDesc();
}
