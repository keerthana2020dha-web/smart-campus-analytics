package com.smartcampus.analytics.dto;

import java.time.Instant;

public record AssignmentSubmissionDTO(
        Long id,
        Integer semester,
        String courseName,
        String courseCode,
        String assignmentTitle,
        String originalFileName,
        Instant uploadedAt) {
}
