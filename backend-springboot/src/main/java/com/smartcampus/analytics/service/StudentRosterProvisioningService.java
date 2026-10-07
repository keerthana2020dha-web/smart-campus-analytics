package com.smartcampus.analytics.service;

import com.smartcampus.analytics.model.StudentAccount;
import com.smartcampus.analytics.repository.StudentAccountRepository;
import com.smartcampus.analytics.repository.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class StudentRosterProvisioningService {

    private final StudentRepository studentRepository;
    private final StudentAccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentRosterProvisioningService(StudentRepository studentRepository,
                                            StudentAccountRepository accountRepository,
                                            PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public int importRoster(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() > 1_000_000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Upload a non-empty roster CSV under 1 MB");
        }
        List<RosterRow> rows = parse(file);
        if (rows.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "The roster contains no student rows");
        }

        for (RosterRow row : rows) {
            if (!studentRepository.existsByStudentId(row.studentId())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Registration number does not match a saved student: " + row.studentId());
            }
        }

        for (RosterRow row : rows) {
            StudentAccount account = accountRepository.findByStudentStudentId(row.studentId())
                    .orElseGet(() -> {
                        StudentAccount created = new StudentAccount();
                        created.setStudent(studentRepository.findByStudentId(row.studentId())
                                .orElseThrow());
                        return created;
                    });
            account.setPasswordHash(passwordEncoder.encode(row.dateOfBirth().toString()));
            account.setFailedAttempts(0);
            account.setLockedUntil(null);
            accountRepository.save(account);
        }
        return rows.size();
    }

    private static List<RosterRow> parse(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String header = reader.readLine();
            if (header == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Roster CSV is empty");
            }
            header = header.replace("\uFEFF", "").trim();
            if (!"studentId,dateOfBirth".equalsIgnoreCase(header)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "CSV header must be studentId,dateOfBirth (DOB format: YYYY-MM-DD)");
            }

            List<RosterRow> rows = new ArrayList<>();
            Set<String> studentIds = new HashSet<>();
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                String[] values = line.split(",", -1);
                if (values.length != 2 || values[0].isBlank() || values[1].isBlank()) {
                    throw invalidRow(lineNumber);
                }
                String studentId = values[0].trim();
                if (!studentIds.add(studentId)) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Duplicate registration number on CSV line " + lineNumber);
                }
                try {
                    rows.add(new RosterRow(studentId, LocalDate.parse(values[1].trim())));
                } catch (DateTimeParseException exception) {
                    throw invalidRow(lineNumber);
                }
                if (rows.size() > 10_000) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "A roster may contain at most 10,000 students");
                }
            }
            return rows;
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Could not read roster CSV", exception);
        }
    }

    private static ResponseStatusException invalidRow(int lineNumber) {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid roster CSV line " + lineNumber + "; expected studentId,YYYY-MM-DD");
    }

    private record RosterRow(String studentId, LocalDate dateOfBirth) {
    }
}
