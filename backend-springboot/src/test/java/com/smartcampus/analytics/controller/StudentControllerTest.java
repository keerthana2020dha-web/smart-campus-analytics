package com.smartcampus.analytics.controller;

import com.smartcampus.analytics.model.Student;
import com.smartcampus.analytics.repository.StudentRepository;
import com.smartcampus.analytics.service.ScoringEngineService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StudentControllerTest {

    private final StudentRepository studentRepository = mock(StudentRepository.class);
    private final ScoringEngineService scoringEngineService = mock(ScoringEngineService.class);
    private final StudentController controller =
            new StudentController(studentRepository, scoringEngineService);

    @Test
    void createsScoredStudentWithCreatedStatus() {
        Student student = student("STU-1");
        when(studentRepository.existsByStudentId("STU-1")).thenReturn(false);
        doNothing().when(scoringEngineService).processStudentRiskAndScore(student);
        when(studentRepository.save(student)).thenReturn(student);

        var response = controller.createStudent(student);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(student, response.getBody());
        verify(scoringEngineService).processStudentRiskAndScore(student);
    }

    @Test
    void rejectsDuplicateStudentIds() {
        Student student = student("STU-1");
        when(studentRepository.existsByStudentId("STU-1")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> controller.createStudent(student));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void returnsNotFoundWhenLookingUpUnknownStudent() {
        when(studentRepository.findByStudentId("missing")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> controller.getStudent("missing"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void doesNotAllowStudentIdChangesDuringUpdate() {
        Student existing = student("STU-1");
        Student update = student("STU-2");
        when(studentRepository.findByStudentId("STU-1")).thenReturn(Optional.of(existing));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.updateStudent("STU-1", update));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    private static Student student(String studentId) {
        Student student = new Student();
        student.setStudentId(studentId);
        student.setName("Test Student");
        return student;
    }
}
