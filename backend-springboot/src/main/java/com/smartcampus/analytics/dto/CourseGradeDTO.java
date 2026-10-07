package com.smartcampus.analytics.dto;

public record CourseGradeDTO(
        Long id, Integer semester, String courseName, String courseCode, String grade) {
}
