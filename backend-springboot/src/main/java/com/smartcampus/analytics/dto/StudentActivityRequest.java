package com.smartcampus.analytics.dto;

import java.time.LocalDate;

public record StudentActivityRequest(
        String activityType,
        String activityName,
        LocalDate activityDate,
        String description) {
}
