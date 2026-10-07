package com.smartcampus.analytics.controller;

import com.smartcampus.analytics.dto.FacultyLoginRequest;
import com.smartcampus.analytics.dto.FacultyLoginResponse;
import com.smartcampus.analytics.service.FacultyAuthService;
import com.smartcampus.analytics.service.FacultyRosterProvisioningService;
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
@RequestMapping("/api/v1/faculty-portal/auth")
public class FacultyAuthController {

    private final FacultyAuthService authService;
    private final FacultyRosterProvisioningService rosterService;
    private final String rosterImportToken;

    public FacultyAuthController(FacultyAuthService authService,
                                 FacultyRosterProvisioningService rosterService,
                                 @Value("${app.faculty-portal.roster-import-token:}")
                                 String rosterImportToken) {
        this.authService = authService;
        this.rosterService = rosterService;
        this.rosterImportToken = rosterImportToken;
    }

    @PostMapping("/login")
    public FacultyLoginResponse login(@RequestBody FacultyLoginRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid faculty credentials");
        }
        return authService.login(request.facultyId(), request.password());
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
                    "Faculty roster import is not authorized");
        }
        return Map.of("accountsProvisioned", rosterService.importRoster(file));
    }
}
