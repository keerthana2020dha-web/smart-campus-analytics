package com.smartcampus.analytics.controller;

import com.smartcampus.analytics.model.Student;
import com.smartcampus.analytics.repository.StudentRepository;
import com.smartcampus.analytics.service.ScoringEngineService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/students")
@CrossOrigin(origins = "*") // Allow all origins for local testing
public class StudentController {

    private final StudentRepository studentRepository;
    private final ScoringEngineService scoringEngineService;

    public StudentController(StudentRepository studentRepository,
                             ScoringEngineService scoringEngineService) {
        this.studentRepository = studentRepository;
        this.scoringEngineService = scoringEngineService;
    }

    @GetMapping
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @GetMapping("/{studentId}")
    public Student getStudent(@PathVariable String studentId) {
        return studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Student not found: " + studentId));
    }

    @PostMapping
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        scoringEngineService.processStudentRiskAndScore(student);
        if (studentRepository.existsByStudentId(student.getStudentId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Student ID already exists: " + student.getStudentId());
        }
        Student savedStudent = studentRepository.save(student);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedStudent);
    }

    @PutMapping("/{studentId}")
    public Student updateStudent(@PathVariable String studentId, @RequestBody Student update) {
        Student existing = studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Student not found: " + studentId));
        if (update.getStudentId() != null && !studentId.equals(update.getStudentId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Student ID cannot be changed");
        }

        update.setId(existing.getId());
        update.setStudentId(studentId);
        scoringEngineService.processStudentRiskAndScore(update);
        return studentRepository.save(update);
    }

    @DeleteMapping("/{studentId}")
    public ResponseEntity<Void> deleteStudent(@PathVariable String studentId) {
        Student existing = studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Student not found: " + studentId));
        studentRepository.delete(existing);
        return ResponseEntity.noContent().build();
    }
}