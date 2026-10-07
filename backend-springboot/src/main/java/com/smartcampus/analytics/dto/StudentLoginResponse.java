package com.smartcampus.analytics.dto;

import java.time.Instant;

public record StudentLoginResponse(String accessToken, Instant expiresAt) {
}
