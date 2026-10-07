package com.smartcampus.analytics.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

@Service
public class StudentUploadStorageService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private final Path uploadDirectory;

    public StudentUploadStorageService(
            @Value("${app.student-portal.upload-dir:uploads/student-portal}")
            String uploadDirectory) {
        this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    public StoredUpload store(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Choose a non-empty PDF, JPG, or PNG file up to 10 MB");
        }

        String originalName = cleanOriginalFileName(file.getOriginalFilename());
        String extension = extensionOf(originalName);
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Could not read the uploaded file", exception);
        }
        validateFileSignature(extension, bytes);

        String checksum = sha256(bytes);
        String storedName = UUID.randomUUID() + extension;
        Path target = uploadDirectory.resolve(storedName).normalize();
        if (!target.getParent().equals(uploadDirectory)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid upload file name");
        }
        try {
            Files.createDirectories(uploadDirectory);
            Files.write(target, bytes);
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Could not save uploaded file", exception);
        }
        return new StoredUpload(storedName, originalName, checksum);
    }

    public Path pathFor(String storedName) {
        Path path = uploadDirectory.resolve(storedName).normalize();
        if (!path.getParent().equals(uploadDirectory)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Uploaded file not found");
        }
        return path;
    }

    public void delete(String storedName) {
        try {
            Files.deleteIfExists(pathFor(storedName));
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Could not remove uploaded file", exception);
        }
    }

    private static String cleanOriginalFileName(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File name is required");
        }
        String baseName = originalName.replace('\\', '/');
        baseName = baseName.substring(baseName.lastIndexOf('/') + 1)
                .replaceAll("[\\p{Cntrl}]", "_");
        if (baseName.length() > 255) {
            baseName = baseName.substring(baseName.length() - 255);
        }
        return baseName;
    }

    private static String extensionOf(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0) {
            throw unsupportedFileType();
        }
        String extension = filename.substring(dot).toLowerCase(Locale.ROOT);
        if (!extension.equals(".pdf") && !extension.equals(".jpg")
                && !extension.equals(".jpeg") && !extension.equals(".png")) {
            throw unsupportedFileType();
        }
        return extension;
    }

    private static void validateFileSignature(String extension, byte[] bytes) {
        boolean valid = switch (extension) {
            case ".pdf" -> startsWith(bytes, new byte[]{'%', 'P', 'D', 'F', '-'});
            case ".png" -> startsWith(bytes,
                    new byte[]{(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10});
            case ".jpg", ".jpeg" -> startsWith(bytes,
                    new byte[]{(byte) 0xff, (byte) 0xd8, (byte) 0xff});
            default -> false;
        };
        if (!valid) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "File contents do not match the selected file type");
        }
    }

    private static boolean startsWith(byte[] content, byte[] signature) {
        if (content.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if (content[i] != signature[i]) {
                return false;
            }
        }
        return true;
    }

    private static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private static ResponseStatusException unsupportedFileType() {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Only PDF, JPG, and PNG files are accepted");
    }

    public record StoredUpload(String storedName, String originalName, String sha256) {
    }
}
