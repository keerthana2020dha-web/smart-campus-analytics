package com.smartcampus.analytics.controller;

import com.smartcampus.analytics.dto.StudentStaffFeedbackRequest;
import com.smartcampus.analytics.model.StudentStaffFeedback;
import com.smartcampus.analytics.repository.StudentStaffFeedbackRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/student-portal/feedback")
public class StudentStaffFeedbackController {

    private final StudentStaffFeedbackRepository feedbackRepository;

    public StudentStaffFeedbackController(StudentStaffFeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    @PostMapping
    public ResponseEntity<Void> submitFeedback(@RequestBody StudentStaffFeedbackRequest request) {
        if (request == null || request.staffName() == null
                || request.staffName().isBlank() || request.staffName().length() > 120) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Staff name is required (maximum 120 characters)");
        }
        validateRating(request.rating());
        validateComment(request.comment());

        StudentStaffFeedback feedback = new StudentStaffFeedback();
        feedback.setStaffName(request.staffName().trim());
        feedback.setRating(request.rating());
        feedback.setComment(normalizeComment(request.comment()));
        feedbackRepository.save(feedback);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    private static void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
        }
    }

    private static void validateComment(String comment) {
        if (comment != null && comment.length() > 1000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Feedback must be 1000 characters or fewer");
        }
    }

    private static String normalizeComment(String comment) {
        return comment == null || comment.isBlank() ? null : comment.trim();
    }
}
