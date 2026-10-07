package com.smartcampus.analytics.repository;

import com.smartcampus.analytics.model.CourseGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseGradeRepository extends JpaRepository<CourseGrade, Long> {
    List<CourseGrade> findAllByStudentStudentIdOrderByCourseNameAsc(String studentId);

    Optional<CourseGrade> findByIdAndStudentStudentId(Long id, String studentId);

    void deleteAllByStudentStudentId(String studentId);
}
