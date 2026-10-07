package com.smartcampus.analytics.dto;

import java.time.Instant;
import java.time.LocalDate;

public record StudentActivityDTO(
        Long id,
        String activityType,
        String activityName,
        LocalDate activityDate,
        String description,
        Instant createdAt) {
}
