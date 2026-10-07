package com.smartcampus.analytics.controller;

import com.smartcampus.analytics.dto.StudentActivityHeartbeatRequest;
import com.smartcampus.analytics.service.StudentPortalActivityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/student-portal/activity")
public class StudentPortalActivityController {

    private final StudentPortalActivityService activityService;

    public StudentPortalActivityController(StudentPortalActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<Void> heartbeat(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestBody StudentActivityHeartbeatRequest request) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }
        activityService.heartbeat(authorization.substring(7).trim(), request.active());
        return ResponseEntity.noContent().build();
    }
}
