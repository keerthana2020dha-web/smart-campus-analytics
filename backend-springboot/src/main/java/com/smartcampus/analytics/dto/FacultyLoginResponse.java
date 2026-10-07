package com.smartcampus.analytics.dto;

import java.time.Instant;

public record FacultyLoginResponse(String accessToken, Instant expiresAt) {
}
