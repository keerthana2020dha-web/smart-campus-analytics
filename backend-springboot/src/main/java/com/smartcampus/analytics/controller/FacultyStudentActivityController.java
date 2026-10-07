package com.smartcampus.analytics.controller;

import com.smartcampus.analytics.dto.WeeklyStudentActivityDTO;
import com.smartcampus.analytics.service.StudentPortalActivityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/faculty-portal/student-activity")
public class FacultyStudentActivityController {

    private final StudentPortalActivityService activityService;

    public FacultyStudentActivityController(StudentPortalActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping("/weekly")
    public List<WeeklyStudentActivityDTO> getWeeklyActivity() {
        return activityService.getWeeklyActivity();
    }
}
