package com.smartcampus.analytics.dto;

public record WeeklyStudentActivityDTO(
        String studentId,
        long loginCount,
        long activeSeconds,
        double activeHours
) {
}
