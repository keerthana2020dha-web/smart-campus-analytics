package com.smartcampus.analytics.repository;

import com.smartcampus.analytics.model.StudentActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentActivityRepository extends JpaRepository<StudentActivity, Long> {
    List<StudentActivity> findAllByStudentStudentIdOrderByActivityDateDescCreatedAtDesc(
            String studentId);

    long countByStudentStudentIdAndActivityType(String studentId, String activityType);
}
