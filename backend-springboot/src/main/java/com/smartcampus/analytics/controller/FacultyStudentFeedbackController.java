package com.smartcampus.analytics.controller;

import com.smartcampus.analytics.dto.FacultyStudentFeedbackRequest;
import com.smartcampus.analytics.dto.FacultyStudentFeedbackView;
import com.smartcampus.analytics.dto.StudentStaffFeedbackView;
import com.smartcampus.analytics.model.FacultyStudentFeedback;
import com.smartcampus.analytics.model.FacultyAccount;
import com.smartcampus.analytics.model.Student;
import com.smartcampus.analytics.repository.FacultyAccountRepository;
import com.smartcampus.analytics.repository.FacultyStudentFeedbackRepository;
import com.smartcampus.analytics.repository.StudentRepository;
import com.smartcampus.analytics.repository.StudentStaffFeedbackRepository;
import com.smartcampus.analytics.security.FacultyPrincipal;
import com.smartcampus.analytics.service.ScoringEngineService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/faculty-portal/feedback")
public class FacultyStudentFeedbackController {

    private final FacultyStudentFeedbackRepository feedbackRepository;
    private final StudentStaffFeedbackRepository studentStaffFeedbackRepository;
    private final StudentRepository studentRepository;
    private final FacultyAccountRepository facultyAccountRepository;
    private final ScoringEngineService scoringEngineService;

    public FacultyStudentFeedbackController(
            FacultyStudentFeedbackRepository feedbackRepository,
            StudentStaffFeedbackRepository studentStaffFeedbackRepository,
            StudentRepository studentRepository,
            FacultyAccountRepository facultyAccountRepository,
            ScoringEngineService scoringEngineService) {
        this.feedbackRepository = feedbackRepository;
        this.studentStaffFeedbackRepository = studentStaffFeedbackRepository;
        this.studentRepository = studentRepository;
        this.facultyAccountRepository = facultyAccountRepository;
        this.scoringEngineService = scoringEngineService;
    }

    @PostMapping
    @Transactional
    public FacultyStudentFeedbackView submitStudentFeedback(
            @AuthenticationPrincipal FacultyPrincipal principal,
            @RequestBody FacultyStudentFeedbackRequest request) {
        if (request == null || request.studentId() == null || request.studentId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student ID is required");
        }
        validateRating(request.rating());
        validateComment(request.comment());

        String studentId = request.studentId().trim();
        Student student = studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Student not found"));
        FacultyAccount faculty = facultyAccountRepository.findByFacultyId(principal.facultyId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Faculty account is not available"));

        FacultyStudentFeedback feedback = feedbackRepository
                .findByStudentStudentIdAndFacultyFacultyId(studentId, principal.facultyId())
                .orElseGet(FacultyStudentFeedback::new);
        feedback.setStudent(student);
        feedback.setFaculty(faculty);
        feedback.setRating(request.rating());
        feedback.setComment(normalizeComment(request.comment()));
        feedback.setUpdatedAt(Instant.now());
        feedbackRepository.save(feedback);

        Double averageRating = feedbackRepository.averageRatingForStudent(studentId);
        if (averageRating != null) {
            student.setFacultyFeedbackScore(averageRating * 20.0);
            scoringEngineService.processStudentRiskAndScore(student);
            studentRepository.save(student);
        }

        return toView(feedback);
    }

    @GetMapping("/mine")
    public List<FacultyStudentFeedbackView> getMyStudentFeedback(
            @AuthenticationPrincipal FacultyPrincipal principal) {
        return feedbackRepository.findAllByFacultyFacultyIdOrderByUpdatedAtDesc(
                        principal.facultyId())
                .stream()
                .map(FacultyStudentFeedbackController::toView)
                .toList();
    }

    @GetMapping("/staff")
    public List<StudentStaffFeedbackView> getAnonymousStaffFeedback() {
        return studentStaffFeedbackRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(feedback -> new StudentStaffFeedbackView(
                        feedback.getStaffName(),
                        feedback.getRating(),
                        feedback.getComment()))
                .toList();
    }

    private static FacultyStudentFeedbackView toView(FacultyStudentFeedback feedback) {
        return new FacultyStudentFeedbackView(
                feedback.getStudent().getStudentId(),
                feedback.getRating(),
                feedback.getComment(),
                feedback.getUpdatedAt());
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
