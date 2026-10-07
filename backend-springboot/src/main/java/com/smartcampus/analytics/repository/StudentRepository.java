package com.smartcampus.analytics.repository;

import com.smartcampus.analytics.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByRiskCategory(String riskCategory);

    Optional<Student> findByStudentId(String studentId);

    boolean existsByStudentId(String studentId);

    boolean existsByStudentIdAndIdNot(String studentId, Long id);
}