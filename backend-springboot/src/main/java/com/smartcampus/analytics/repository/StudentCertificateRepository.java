package com.smartcampus.analytics.repository;

import com.smartcampus.analytics.model.StudentCertificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentCertificateRepository extends JpaRepository<StudentCertificate, Long> {
    List<StudentCertificate> findAllByStudentStudentIdOrderByUploadedAtDesc(String studentId);

    Optional<StudentCertificate> findByIdAndStudentStudentId(Long id, String studentId);

    boolean existsByStudentStudentIdAndSha256(String studentId, String sha256);

    long countByStudentStudentId(String studentId);
}
