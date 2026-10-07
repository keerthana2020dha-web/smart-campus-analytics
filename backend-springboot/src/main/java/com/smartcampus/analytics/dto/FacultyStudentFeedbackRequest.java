package com.smartcampus.analytics.dto;

public record FacultyStudentFeedbackRequest(String studentId, Integer rating, String comment) {
}
