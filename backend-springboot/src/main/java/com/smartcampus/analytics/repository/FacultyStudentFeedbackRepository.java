package com.smartcampus.analytics.repository;

import com.smartcampus.analytics.model.FacultyStudentFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FacultyStudentFeedbackRepository
        extends JpaRepository<FacultyStudentFeedback, Long> {

    Optional<FacultyStudentFeedback> findByStudentStudentIdAndFacultyFacultyId(
            String studentId, String facultyId);

    List<FacultyStudentFeedback> findAllByFacultyFacultyIdOrderByUpdatedAtDesc(String facultyId);

    @Query("select avg(feedback.rating) from FacultyStudentFeedback feedback "
            + "where feedback.student.studentId = :studentId")
    Double averageRatingForStudent(@Param("studentId") String studentId);
}
