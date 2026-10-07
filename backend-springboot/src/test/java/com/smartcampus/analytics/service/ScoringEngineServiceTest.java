package com.smartcampus.analytics.service;

import com.smartcampus.analytics.model.Student;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScoringEngineServiceTest {

    private final ScoringEngineService scoringEngineService = new ScoringEngineService();

    @Test
    void scoresLegacyStudentWithAvailableCategoriesAndReportsCoverage() {
        Student student = new Student();
        student.setStudentId("S-1001");
        student.setName("Test Student");
        student.setGpa(7.5);
        student.setAttendancePct(80.0);
        student.setLmsActivityHrs(12);
        student.setPlacementMockScore(70);

        scoringEngineService.processStudentRiskAndScore(student);

        assertEquals(72.9, student.getSuccessScore());
        assertEquals("Moderate", student.getRiskCategory());
        assertEquals("Low", student.getAcademicRisk());
        assertEquals(57.1, student.getDataCoveragePct());
    }

    @Test
    void combinesAllSevenCategoriesAndCreatesActionableRiskSignals() {
        Student student = new Student();
        student.setStudentId("S-1002");
        student.setName("Test Student");
        student.setGpa(8.0);
        student.setInternalAssessmentAvg(80.0);
        student.setSubjectPerformanceAvg(70.0);
        student.setBacklogs(0);
        student.setAttendancePct(90.0);
        student.setLmsActivityHrs(20);
        student.setLmsLoginFrequency(10);
        student.setAssignmentCompletionPct(90.0);
        student.setEventsParticipated(10);
        student.setClubsParticipated(3);
        student.setHackathonsParticipated(3);
        student.setCertifications(5);
        student.setAptitudeScore(80.0);
        student.setCodingScore(70.0);
        student.setMockInterviewScore(60.0);
        student.setTechnicalSkillScore(80.0);
        student.setSoftSkillScore(70.0);
        student.setStudentSatisfactionScore(80.0);
        student.setFacultyFeedbackScore(90.0);

        scoringEngineService.processStudentRiskAndScore(student);

        assertEquals(84.4, student.getSuccessScore());
        assertEquals(82.5, student.getAcademicScore());
        assertEquals(70.0, student.getPlacementReadinessScore());
        assertEquals("Low", student.getAcademicRisk());
        assertEquals("Low", student.getPlacementRisk());
        assertEquals(100.0, student.getDataCoveragePct());
        assertEquals("Low", student.getRiskCategory());
    }

    @Test
    void handlesMissingIndicatorsWithoutInventingScores() {
        Student student = new Student();
        student.setStudentId("S-1003");
        student.setName("Test Student");

        scoringEngineService.processStudentRiskAndScore(student);

        assertNull(student.getSuccessScore());
        assertNull(student.getAcademicScore());
        assertEquals("Insufficient data", student.getRiskCategory());
        assertEquals(0.0, student.getDataCoveragePct());
        assertEquals("Insufficient data", student.getAcademicRisk());
        assertEquals("Insufficient data", student.getPlacementRisk());
    }

    @Test
    void raisesAcademicAndPlacementFlagsForLowIndicators() {
        Student student = new Student();
        student.setStudentId("S-1004");
        student.setName("At Risk Student");
        student.setGpa(5.0);
        student.setBacklogs(2);
        student.setAttendancePct(60.0);
        student.setAptitudeScore(40.0);
        student.setCodingScore(45.0);
        student.setMockInterviewScore(50.0);

        scoringEngineService.processStudentRiskAndScore(student);

        assertEquals("High", student.getAcademicRisk());
        assertEquals("High", student.getPlacementRisk());
        assertEquals("High", student.getRiskCategory());
    }

    @Test
    void rejectsIndicatorValuesOutsideTheirSupportedRange() {
        Student student = new Student();
        student.setStudentId("S-1005");
        student.setName("Invalid Student");
        student.setGpa(10.1);

        assertThrows(ResponseStatusException.class,
                () -> scoringEngineService.processStudentRiskAndScore(student));
    }
}
