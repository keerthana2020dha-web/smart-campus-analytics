package com.smartcampus.analytics.dto;

import java.time.Instant;

public record FacultyStudentFeedbackView(
        String studentId,
        Integer rating,
        String comment,
        Instant updatedAt) {
}
