package com.smartcampus.analytics.service;

import com.smartcampus.analytics.model.Student;
import com.smartcampus.analytics.model.StudentAccount;
import com.smartcampus.analytics.repository.StudentAccountRepository;
import com.smartcampus.analytics.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudentRosterProvisioningServiceTest {

    private StudentRepository studentRepository;
    private StudentAccountRepository accountRepository;
    private PasswordEncoder passwordEncoder;
    private StudentRosterProvisioningService rosterService;
    private Student student;

    @BeforeEach
    void setUp() {
        studentRepository = mock(StudentRepository.class);
        accountRepository = mock(StudentAccountRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        rosterService = new StudentRosterProvisioningService(
                studentRepository, accountRepository, passwordEncoder);
        student = new Student();
        student.setStudentId("STU-101");
        when(studentRepository.existsByStudentId("STU-101")).thenReturn(true);
        when(studentRepository.findByStudentId("STU-101")).thenReturn(Optional.of(student));
        when(accountRepository.findByStudentStudentId("STU-101"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("2005-04-12")).thenReturn("$2a$test-hash");
    }

    @Test
    void rosterCreatesAccountWithOnlyEncodedDateOfBirth() {
        var file = csv("studentId,dateOfBirth\nSTU-101,2005-04-12\n");

        assertEquals(1, rosterService.importRoster(file));

        var accountCaptor = org.mockito.ArgumentCaptor.forClass(StudentAccount.class);
        verify(accountRepository).save(accountCaptor.capture());
        assertEquals(student, accountCaptor.getValue().getStudent());
        assertEquals("$2a$test-hash", accountCaptor.getValue().getPasswordHash());
        assertNotEquals("2005-04-12", accountCaptor.getValue().getPasswordHash());
        verify(passwordEncoder).encode("2005-04-12");
    }

    @Test
    void malformedDateIsRejectedBeforeAnyAccountIsSaved() {
        var file = csv("studentId,dateOfBirth\nSTU-101,12-04-2005\n");

        assertThrows(ResponseStatusException.class, () -> rosterService.importRoster(file));
        verify(accountRepository, never()).save(any());
    }

    @Test
    void rosterCannotProvisionAnUnknownStudent() {
        when(studentRepository.existsByStudentId("UNKNOWN")).thenReturn(false);
        var file = csv("studentId,dateOfBirth\nUNKNOWN,2005-04-12\n");

        assertThrows(ResponseStatusException.class, () -> rosterService.importRoster(file));
        verify(accountRepository, never()).save(any());
    }

    private static MockMultipartFile csv(String value) {
        return new MockMultipartFile(
                "file", "roster.csv", "text/csv", value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
