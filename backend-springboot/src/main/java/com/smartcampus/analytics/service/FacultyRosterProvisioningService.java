package com.smartcampus.analytics.service;

import com.smartcampus.analytics.model.FacultyAccount;
import com.smartcampus.analytics.repository.FacultyAccountRepository;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class FacultyRosterProvisioningService {

    private final FacultyAccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public FacultyRosterProvisioningService(FacultyAccountRepository accountRepository,
                                            PasswordEncoder passwordEncoder) {
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
                    HttpStatus.BAD_REQUEST, "The roster contains no faculty rows");
        }
        for (RosterRow row : rows) {
            FacultyAccount account = accountRepository.findByFacultyId(row.facultyId())
                    .orElseGet(FacultyAccount::new);
            account.setFacultyId(row.facultyId());
            account.setPasswordHash(passwordEncoder.encode(row.password()));
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
            if (header == null || !"facultyId,password".equalsIgnoreCase(
                    header.replace("\uFEFF", "").trim())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "CSV header must be facultyId,password");
            }
            List<RosterRow> rows = new ArrayList<>();
            Set<String> facultyIds = new HashSet<>();
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                String[] values = line.split(",", -1);
                if (values.length != 2 || values[0].isBlank()
                        || values[1].isBlank() || values[1].length() < 12) {
                    throw invalidRow(lineNumber);
                }
                String facultyId = values[0].trim();
                if (facultyId.length() > 50 || !facultyIds.add(facultyId)) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Invalid or duplicate faculty ID on CSV line " + lineNumber);
                }
                rows.add(new RosterRow(facultyId, values[1]));
                if (rows.size() > 2_000) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "A roster may contain at most 2,000 faculty");
                }
            }
            return rows;
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Could not read faculty roster CSV", exception);
        }
    }

    private static ResponseStatusException invalidRow(int lineNumber) {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid faculty roster CSV line " + lineNumber
                        + "; expected facultyId and a password of at least 12 characters");
    }

    private record RosterRow(String facultyId, String password) {
    }
}
