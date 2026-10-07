package com.smartcampus.analytics.security;

import com.smartcampus.analytics.model.StudentSession;
import com.smartcampus.analytics.repository.StudentSessionRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class StudentSessionService {

    private final StudentSessionRepository sessionRepository;

    public StudentSessionService(StudentSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public Optional<String> studentIdForToken(String token) {
        return sessionRepository.findByTokenHashAndExpiresAtAfter(hash(token), java.time.Instant.now())
                .map(StudentSession::getStudentId);
    }

    public void revoke(String token) {
        sessionRepository.deleteByTokenHash(hash(token));
    }

    public static String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
