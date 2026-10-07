package com.smartcampus.analytics.controller;

import com.smartcampus.analytics.dto.*;
import com.smartcampus.analytics.model.*;
import com.smartcampus.analytics.repository.*;
import com.smartcampus.analytics.security.StudentPrincipal;
import com.smartcampus.analytics.service.ScoringEngineService;
import com.smartcampus.analytics.service.StudentUploadStorageService;
import com.smartcampus.analytics.service.StudentUploadStorageService.StoredUpload;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1/student-portal/me")
public class StudentPortalController {

    private static final List<String> ACTIVITY_TYPES = List.of("HACKATHON", "EVENT", "CLUB");
    private static final List<String> CERTIFICATE_TYPES =
            List.of("CERTIFICATION", "COURSE", "HACKATHON", "EVENT", "CLUB", "OTHER");

    private final StudentRepository studentRepository;
    private final StudentAccountRepository accountRepository;
    private final StudentActivityRepository activityRepository;
    private final StudentCertificateRepository certificateRepository;
    private final AssignmentSubmissionRepository assignmentRepository;
    private final ScoringEngineService scoringEngineService;
    private final StudentUploadStorageService storageService;

    public StudentPortalController(StudentRepository studentRepository,
                                   StudentAccountRepository accountRepository,
                                   StudentActivityRepository activityRepository,
                                   StudentCertificateRepository certificateRepository,
                                   AssignmentSubmissionRepository assignmentRepository,
                                   ScoringEngineService scoringEngineService,
                                   StudentUploadStorageService storageService) {
        this.studentRepository = studentRepository;
        this.accountRepository = accountRepository;
        this.activityRepository = activityRepository;
        this.certificateRepository = certificateRepository;
        this.assignmentRepository = assignmentRepository;
        this.scoringEngineService = scoringEngineService;
        this.storageService = storageService;
    }

    @GetMapping
    public StudentProfileDTO getMyProfile(@AuthenticationPrincipal StudentPrincipal principal) {
        Student student = requireStudent(principal);
        StudentAccount account = accountRepository.findByStudentStudentId(principal.studentId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Student account is not available"));
        return new StudentProfileDTO(
                student.getStudentId(),
                student.getName(),
                student.getDepartment(),
                student.getSemester(),
                account.getSuccessfulLoginCount(),
                activityRepository.countByStudentStudentIdAndActivityType(
                        principal.studentId(), "HACKATHON"),
                activityRepository.countByStudentStudentIdAndActivityType(
                        principal.studentId(), "EVENT"),
                activityRepository.countByStudentStudentIdAndActivityType(
                        principal.studentId(), "CLUB"),
                certificateRepository.countByStudentStudentId(principal.studentId()),
                assignmentRepository.countByStudentStudentId(principal.studentId()),
                account.getLastLoginAt());
    }

    @GetMapping("/activities")
    public List<StudentActivityDTO> getMyActivities(
            @AuthenticationPrincipal StudentPrincipal principal) {
        return activityRepository
                .findAllByStudentStudentIdOrderByActivityDateDescCreatedAtDesc(principal.studentId())
                .stream()
                .map(StudentPortalController::toActivityDTO)
                .toList();
    }

    @PostMapping("/activities")
    @Transactional
    public StudentActivityDTO addActivity(
            @AuthenticationPrincipal StudentPrincipal principal,
            @RequestBody StudentActivityRequest request) {
        validateActivity(request);
        Student student = requireStudent(principal);
        String type = request.activityType().trim().toUpperCase(Locale.ROOT);
        StudentActivity activity = new StudentActivity();
        activity.setStudent(student);
        activity.setActivityType(type);
        activity.setActivityName(request.activityName().trim());
        activity.setActivityDate(request.activityDate());
        activity.setDescription(trimToNull(request.description()));
        StudentActivity saved = activityRepository.save(activity);
        refreshActivityScores(student, principal.studentId());
        return toActivityDTO(saved);
    }

    @GetMapping("/certificates")
    public List<StudentCertificateDTO> getMyCertificates(
            @AuthenticationPrincipal StudentPrincipal principal) {
        return certificateRepository
                .findAllByStudentStudentIdOrderByUploadedAtDesc(principal.studentId())
                .stream()
                .map(StudentPortalController::toCertificateDTO)
                .toList();
    }

    @PostMapping(value = "/certificates", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public StudentCertificateDTO uploadCertificate(
            @AuthenticationPrincipal StudentPrincipal principal,
            @RequestParam String title,
            @RequestParam String certificateType,
            @RequestParam(required = false) LocalDate issuedDate,
            @RequestPart("file") MultipartFile file) {
        String cleanTitle = requireText(title, "Certificate title", 160);
        String type = normalizeChoice(certificateType, CERTIFICATE_TYPES, "Certificate type");
        Student student = requireStudent(principal);
        StoredUpload upload = storageService.store(file);
        if (certificateRepository.existsByStudentStudentIdAndSha256(
                principal.studentId(), upload.sha256())) {
            storageService.delete(upload.storedName());
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "This certificate file was already uploaded");
        }

        StudentCertificate certificate = new StudentCertificate();
        certificate.setStudent(student);
        certificate.setTitle(cleanTitle);
        certificate.setCertificateType(type);
        certificate.setIssuedDate(issuedDate);
        certificate.setOriginalFileName(upload.originalName());
        certificate.setStoredFileName(upload.storedName());
        certificate.setSha256(upload.sha256());
        try {
            StudentCertificate saved = certificateRepository.save(certificate);
            refreshActivityScores(student, principal.studentId());
            return toCertificateDTO(saved);
        } catch (RuntimeException exception) {
            storageService.delete(upload.storedName());
            throw exception;
        }
    }

    @GetMapping("/certificates/{certificateId}/file")
    public ResponseEntity<Resource> downloadCertificate(
            @AuthenticationPrincipal StudentPrincipal principal,
            @PathVariable Long certificateId) {
        StudentCertificate certificate = certificateRepository
                .findByIdAndStudentStudentId(certificateId, principal.studentId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Certificate not found"));
        return fileResponse(certificate.getStoredFileName(), certificate.getOriginalFileName());
    }

    @GetMapping("/assignment-submissions")
    public List<AssignmentSubmissionDTO> getMyAssignmentSubmissions(
            @AuthenticationPrincipal StudentPrincipal principal) {
        return assignmentRepository
                .findAllByStudentStudentIdOrderByUploadedAtDesc(principal.studentId())
                .stream()
                .map(StudentPortalController::toAssignmentDTO)
                .toList();
    }

    @PostMapping(
            value = "/assignment-submissions",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AssignmentSubmissionDTO submitAssignment(
            @AuthenticationPrincipal StudentPrincipal principal,
            @RequestParam Integer semester,
            @RequestParam String courseName,
            @RequestParam(required = false) String courseCode,
            @RequestParam String assignmentTitle,
            @RequestPart("file") MultipartFile file) {
        if (semester == null || semester < 1 || semester > 8) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Semester must be between 1 and 8");
        }
        Student student = requireStudent(principal);
        AssignmentSubmission submission = new AssignmentSubmission();
        submission.setStudent(student);
        submission.setSemester(semester);
        submission.setCourseName(requireText(courseName, "Course name", 120));
        submission.setCourseCode(trimToNull(courseCode));
        if (submission.getCourseCode() != null && submission.getCourseCode().length() > 30) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Course code must be 30 characters or fewer");
        }
        submission.setAssignmentTitle(requireText(assignmentTitle, "Assignment title", 160));
        StoredUpload upload = storageService.store(file);
        submission.setOriginalFileName(upload.originalName());
        submission.setStoredFileName(upload.storedName());
        try {
            return toAssignmentDTO(assignmentRepository.save(submission));
        } catch (RuntimeException exception) {
            storageService.delete(upload.storedName());
            throw exception;
        }
    }

    @GetMapping("/assignment-submissions/{submissionId}/file")
    public ResponseEntity<Resource> downloadAssignment(
            @AuthenticationPrincipal StudentPrincipal principal,
            @PathVariable Long submissionId) {
        AssignmentSubmission submission = assignmentRepository
                .findByIdAndStudentStudentId(submissionId, principal.studentId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Assignment submission not found"));
        return fileResponse(submission.getStoredFileName(), submission.getOriginalFileName());
    }

    private Student requireStudent(StudentPrincipal principal) {
        return studentRepository.findByStudentId(principal.studentId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Student account is not available"));
    }

    private void refreshActivityScores(Student student, String studentId) {
        student.setHackathonsParticipated(Math.toIntExact(
                activityRepository.countByStudentStudentIdAndActivityType(studentId, "HACKATHON")));
        student.setEventsParticipated(Math.toIntExact(
                activityRepository.countByStudentStudentIdAndActivityType(studentId, "EVENT")));
        student.setClubsParticipated(Math.toIntExact(
                activityRepository.countByStudentStudentIdAndActivityType(studentId, "CLUB")));
        student.setCertifications(Math.toIntExact(
                certificateRepository.countByStudentStudentId(studentId)));
        scoringEngineService.processStudentRiskAndScore(student);
        studentRepository.save(student);
    }

    private ResponseEntity<Resource> fileResponse(String storedName, String originalName) {
        var path = storageService.pathFor(storedName);
        if (!java.nio.file.Files.isRegularFile(path)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Uploaded file not found");
        }
        try {
            Resource resource = new FileSystemResource(path);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.attachment().filename(originalName).build().toString())
                    .body(resource);
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Could not read uploaded file", exception);
        }
    }

    private static void validateActivity(StudentActivityRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Activity is required");
        }
        normalizeChoice(request.activityType(), ACTIVITY_TYPES, "Activity type");
        requireText(request.activityName(), "Activity name", 160);
        if (request.description() != null && request.description().length() > 500) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Description must be 500 characters or fewer");
        }
    }

    private static String requireText(String value, String label, int maxLength) {
        if (value == null || value.isBlank() || value.trim().length() > maxLength) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    label + " is required and must be " + maxLength + " characters or fewer");
        }
        return value.trim();
    }

    private static String normalizeChoice(String value, List<String> allowed, String label) {
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, label + " is required");
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (!allowed.contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid " + label);
        }
        return normalized;
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static StudentActivityDTO toActivityDTO(StudentActivity activity) {
        return new StudentActivityDTO(
                activity.getId(),
                activity.getActivityType(),
                activity.getActivityName(),
                activity.getActivityDate(),
                activity.getDescription(),
                activity.getCreatedAt());
    }

    private static StudentCertificateDTO toCertificateDTO(StudentCertificate certificate) {
        return new StudentCertificateDTO(
                certificate.getId(),
                certificate.getTitle(),
                certificate.getCertificateType(),
                certificate.getIssuedDate(),
                certificate.getOriginalFileName(),
                certificate.getUploadedAt());
    }

    private static AssignmentSubmissionDTO toAssignmentDTO(AssignmentSubmission submission) {
        return new AssignmentSubmissionDTO(
                submission.getId(),
                submission.getSemester(),
                submission.getCourseName(),
                submission.getCourseCode(),
                submission.getAssignmentTitle(),
                submission.getOriginalFileName(),
                submission.getUploadedAt());
    }
}
