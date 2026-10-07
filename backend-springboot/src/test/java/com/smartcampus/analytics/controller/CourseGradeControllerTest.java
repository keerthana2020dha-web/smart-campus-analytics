package com.smartcampus.analytics.controller;

import com.smartcampus.analytics.dto.CourseGradeDTO;
import com.smartcampus.analytics.model.CourseGrade;
import com.smartcampus.analytics.model.Student;
import com.smartcampus.analytics.repository.CourseGradeRepository;
import com.smartcampus.analytics.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CourseGradeControllerTest {

    private final StudentRepository studentRepository = mock(StudentRepository.class);
    private final CourseGradeRepository courseGradeRepository = mock(CourseGradeRepository.class);
    private final CourseGradeController controller =
            new CourseGradeController(studentRepository, courseGradeRepository);

    @Test
    void addsCourseGradeForSelectedStudent() {
        Student student = student("STU-1");
        when(studentRepository.findByStudentId("STU-1")).thenReturn(Optional.of(student));
        when(courseGradeRepository.save(any(CourseGrade.class))).thenAnswer(invocation -> {
            CourseGrade saved = invocation.getArgument(0);
            saved.setId(7L);
            return saved;
        });

        var response = controller.addCourseGrade(
                "STU-1", new CourseGradeDTO(null, 3, "Data Structures", "CS201", "A"));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(
                new CourseGradeDTO(7L, 3, "Data Structures", "CS201", "A"),
                response.getBody());
        verify(courseGradeRepository).save(any(CourseGrade.class));
    }

    @Test
    void rejectsCourseGradeForUnknownStudent() {
        when(studentRepository.findByStudentId("missing")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.addCourseGrade(
                        "missing", new CourseGradeDTO(null, 1, "Mathematics", null, "A")));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void rejectsBlankCourseOrGrade() {
        when(studentRepository.findByStudentId("STU-1"))
                .thenReturn(Optional.of(student("STU-1")));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.addCourseGrade(
                        "STU-1", new CourseGradeDTO(null, 1, "  ", null, "")));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void rejectsSemesterOutsideStudentProgramRange() {
        when(studentRepository.findByStudentId("STU-1"))
                .thenReturn(Optional.of(student("STU-1")));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.addCourseGrade(
                        "STU-1", new CourseGradeDTO(null, 9, "Mathematics", null, "A")));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    private static Student student(String studentId) {
        Student student = new Student();
        student.setStudentId(studentId);
        student.setName("Test Student");
        return student;
    }
}
