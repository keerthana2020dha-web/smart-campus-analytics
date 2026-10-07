package com.smartcampus.analytics.dto;

import java.time.Instant;
import java.time.LocalDate;

public record StudentCertificateDTO(
        Long id,
        String title,
        String certificateType,
        LocalDate issuedDate,
        String originalFileName,
        Instant uploadedAt) {
}
