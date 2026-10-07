package com.smartcampus.analytics.dto;

import java.time.Instant;

public record StudentProfileDTO(
        String studentId,
        String name,
        String department,
        Integer semester,
        long loginCount,
        long hackathonCount,
        long eventCount,
        long clubCount,
        long certificateCount,
        long assignmentSubmissionCount,
        Instant lastLoginAt) {
}
