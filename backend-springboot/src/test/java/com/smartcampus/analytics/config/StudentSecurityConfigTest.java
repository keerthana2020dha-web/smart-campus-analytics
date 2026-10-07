package com.smartcampus.analytics.config;

import com.smartcampus.analytics.security.StudentPrincipal;
import com.smartcampus.analytics.security.StudentSessionService;
import com.smartcampus.analytics.security.FacultyPrincipal;
import com.smartcampus.analytics.security.FacultySessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StudentSecurityConfigTest.TestEndpoints.class)
@Import({StudentSecurityConfig.class, StudentSecurityConfigTest.TestEndpoints.class})
class StudentSecurityConfigTest {

    private static final String TOKEN = "a".repeat(43);
    private static final String FACULTY_TOKEN = "b".repeat(43);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentSessionService sessionService;

    @MockBean
    private FacultySessionService facultySessionService;

    @Test
    void studentPortalRequiresValidStudentSession() throws Exception {
        when(sessionService.studentIdForToken(TOKEN)).thenReturn(Optional.of("STU-101"));

        mockMvc.perform(get("/api/v1/student-portal/me"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/student-portal/me")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string("STU-101"));
    }

    @Test
    void studentSessionCannotAccessAdministrativeApi() throws Exception {
        when(sessionService.studentIdForToken(TOKEN)).thenReturn(Optional.of("STU-101"));

        mockMvc.perform(get("/api/v1/students")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isForbidden());
    }

    @Test
    void facultySessionCanAccessAnalyticsButNotStudentPortal() throws Exception {
        when(facultySessionService.facultyIdForToken(FACULTY_TOKEN))
                .thenReturn(Optional.of("FAC-101"));

        mockMvc.perform(get("/api/v1/students")
                        .header("Authorization", "Bearer " + FACULTY_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string("FAC-101"));
        mockMvc.perform(get("/api/v1/student-portal/me")
                        .header("Authorization", "Bearer " + FACULTY_TOKEN))
                .andExpect(status().isForbidden());
    }

    @RestController
    static class TestEndpoints {

        @GetMapping("/api/v1/student-portal/me")
        String studentPortal(@AuthenticationPrincipal StudentPrincipal principal) {
            return principal.studentId();
        }

        @GetMapping("/api/v1/students")
        String adminApi(@AuthenticationPrincipal FacultyPrincipal principal) {
            return principal.facultyId();
        }
    }
}
