package com.smartcampus.analytics.repository;

import com.smartcampus.analytics.model.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {
    List<AssignmentSubmission> findAllByStudentStudentIdOrderByUploadedAtDesc(String studentId);

    Optional<AssignmentSubmission> findByIdAndStudentStudentId(Long id, String studentId);

    long countByStudentStudentId(String studentId);
}
