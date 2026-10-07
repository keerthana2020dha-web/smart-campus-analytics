package com.smartcampus.analytics.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "student_certificates", uniqueConstraints = @UniqueConstraint(
        name = "uk_student_certificate_checksum",
        columnNames = {"student_id", "sha256"}), indexes = @Index(
        name = "idx_student_certificates_student_id", columnList = "student_id"))
@Getter
@Setter
@NoArgsConstructor
public class StudentCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id", nullable = false)
    private Student student;

    @Column(name = "certificate_title", nullable = false, length = 160)
    private String title;

    @Column(name = "certificate_type", nullable = false, length = 30)
    private String certificateType;

    @Column(name = "issued_date")
    private LocalDate issuedDate;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "stored_file_name", nullable = false, length = 80)
    private String storedFileName;

    @Column(name = "sha256", nullable = false, length = 64)
    private String sha256;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt = Instant.now();
}
