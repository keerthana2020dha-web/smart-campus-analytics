package com.smartcampus.analytics.controller;

import com.smartcampus.analytics.dto.StudentLoginRequest;
import com.smartcampus.analytics.dto.StudentLoginResponse;
import com.smartcampus.analytics.service.StudentAuthService;
import com.smartcampus.analytics.service.StudentRosterProvisioningService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/student-portal/auth")
public class StudentAuthController {

    private final StudentAuthService authService;
    private final StudentRosterProvisioningService rosterService;
    private final String rosterImportToken;

    public StudentAuthController(StudentAuthService authService,
                                 StudentRosterProvisioningService rosterService,
                                 @Value("${app.student-portal.roster-import-token:}")
                                 String rosterImportToken) {
        this.authService = authService;
        this.rosterService = rosterService;
        this.rosterImportToken = rosterImportToken;
    }

    @PostMapping("/login")
    public StudentLoginResponse login(@RequestBody StudentLoginRequest request) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid registration number or password");
        }
        return authService.login(request.studentId(), request.password());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            authService.logout(authorization.substring(7).trim());
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/roster", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Integer> importRoster(
            @RequestHeader(name = "X-Roster-Import-Token", required = false) String token,
            @RequestPart("file") MultipartFile file) {
        if (rosterImportToken.isBlank() || token == null
                || !MessageDigest.isEqual(
                        rosterImportToken.getBytes(StandardCharsets.UTF_8),
                        token.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(
                    rosterImportToken.isBlank()
                            ? HttpStatus.SERVICE_UNAVAILABLE : HttpStatus.FORBIDDEN,
                    "Roster import is not authorized");
        }
        return Map.of("accountsProvisioned", rosterService.importRoster(file));
    }
}
