package com.smartcampus.analytics.controller;

import com.smartcampus.analytics.dto.CourseGradeDTO;
import com.smartcampus.analytics.model.CourseGrade;
import com.smartcampus.analytics.model.Student;
import com.smartcampus.analytics.repository.CourseGradeRepository;
import com.smartcampus.analytics.repository.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/students/{studentId}/courses")
@CrossOrigin(origins = "*")
public class CourseGradeController {

    private final StudentRepository studentRepository;
    private final CourseGradeRepository courseGradeRepository;

    public CourseGradeController(StudentRepository studentRepository,
                                 CourseGradeRepository courseGradeRepository) {
        this.studentRepository = studentRepository;
        this.courseGradeRepository = courseGradeRepository;
    }

    @GetMapping
    public List<CourseGradeDTO> getCourseGrades(@PathVariable String studentId) {
        requireStudent(studentId);
        return courseGradeRepository.findAllByStudentStudentIdOrderByCourseNameAsc(studentId)
                .stream()
                .map(CourseGradeController::toDTO)
                .toList();
    }

    @PostMapping
    public ResponseEntity<CourseGradeDTO> addCourseGrade(
            @PathVariable String studentId,
            @RequestBody CourseGradeDTO request) {
        Student student = requireStudent(studentId);
        validate(request);

        CourseGrade courseGrade = new CourseGrade();
        courseGrade.setStudent(student);
        courseGrade.setSemester(request.semester());
        courseGrade.setCourseName(request.courseName().trim());
        courseGrade.setCourseCode(trimToNull(request.courseCode()));
        courseGrade.setGrade(request.grade().trim());
        CourseGrade saved = courseGradeRepository.save(courseGrade);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(saved));
    }

    @PutMapping("/{courseGradeId}")
    public CourseGradeDTO updateCourseGrade(
            @PathVariable String studentId,
            @PathVariable Long courseGradeId,
            @RequestBody CourseGradeDTO request) {
        validate(request);
        CourseGrade existing = courseGradeRepository
                .findByIdAndStudentStudentId(courseGradeId, studentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Course grade not found: " + courseGradeId));
        existing.setCourseName(request.courseName().trim());
        existing.setSemester(request.semester());
        existing.setCourseCode(trimToNull(request.courseCode()));
        existing.setGrade(request.grade().trim());
        return toDTO(courseGradeRepository.save(existing));
    }

    @DeleteMapping("/{courseGradeId}")
    public ResponseEntity<Void> deleteCourseGrade(
            @PathVariable String studentId,
            @PathVariable Long courseGradeId) {
        CourseGrade existing = courseGradeRepository
                .findByIdAndStudentStudentId(courseGradeId, studentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Course grade not found: " + courseGradeId));
        courseGradeRepository.delete(existing);
        return ResponseEntity.noContent().build();
    }

    private Student requireStudent(String studentId) {
        return studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Student not found: " + studentId));
    }

    private static void validate(CourseGradeDTO request) {
        if (request == null || request.semester() == null
                || request.semester() < 1 || request.semester() > 8) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Semester must be between 1 and 8");
        }
        if (request.courseName() == null || request.courseName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course name is required");
        }
        if (request.courseName().trim().length() > 120) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Course name must be 120 characters or fewer");
        }
        if (request.courseCode() != null && request.courseCode().trim().length() > 30) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Course code must be 30 characters or fewer");
        }
        if (request.grade() == null || request.grade().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Grade is required");
        }
        if (request.grade().trim().length() > 30) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Grade must be 30 characters or fewer");
        }
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static CourseGradeDTO toDTO(CourseGrade courseGrade) {
        return new CourseGradeDTO(
                courseGrade.getId(),
                courseGrade.getSemester(),
                courseGrade.getCourseName(),
                courseGrade.getCourseCode(),
                courseGrade.getGrade());
    }
}
