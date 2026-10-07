package com.smartcampus.analytics.service;

import com.smartcampus.analytics.model.Student;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class ScoringEngineService {

    public void processStudentRiskAndScore(Student student) {
        if (student.getStudentId() == null || student.getStudentId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "studentId is required");
        }
        if (student.getName() == null || student.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required");
        }
        validateInput(student);
        Double academic = average(
                scale(student.getGpa(), 10.0),
                student.getInternalAssessmentAvg(),
                student.getSubjectPerformanceAvg(),
                student.getBacklogs() == null ? null
                        : Math.max(0.0, 100.0 - student.getBacklogs() * 20.0)
        );
        Double attendance = average(
                student.getAttendancePct(),
                student.getSubjectAttendanceAvg()
        );
        Double lms = average(
                student.getLmsActivityHrs() == null ? null
                        : Math.min(student.getLmsActivityHrs() / 20.0, 1.0) * 100.0,
                student.getLmsLoginFrequency() == null ? null
                        : Math.min(student.getLmsLoginFrequency() / 10.0, 1.0) * 100.0,
                student.getAssignmentCompletionPct()
        );
        Double engagement = average(
                countScore(student.getEventsParticipated(), 10),
                countScore(student.getClubsParticipated(), 3),
                countScore(student.getHackathonsParticipated(), 3),
                countScore(student.getCertifications(), 5)
        );
        Double placement = average(
                student.getAptitudeScore(),
                student.getCodingScore(),
                student.getMockInterviewScore() != null
                        ? student.getMockInterviewScore()
                        : student.getPlacementMockScore() == null ? null
                                : student.getPlacementMockScore().doubleValue()
        );
        Double skills = average(student.getTechnicalSkillScore(), student.getSoftSkillScore());
        Double feedback = average(
                student.getStudentSatisfactionScore(),
                student.getFacultyFeedbackScore()
        );

        student.setAcademicScore(round(academic));
        student.setAttendanceScore(round(attendance));
        student.setLmsScore(round(lms));
        student.setEngagementScore(round(engagement));
        student.setPlacementReadinessScore(round(placement));
        student.setSkillsScore(round(skills));
        student.setFeedbackScore(round(feedback));
        student.setAcademicRisk(academicRiskFor(academic, student.getBacklogs(), attendance));
        student.setPlacementRisk(riskFor(placement, null, 55.0, 70.0));

        double weightedTotal = 0.0;
        double availableWeight = 0.0;
        int availableCategories = 0;
        Double[] scores = {academic, attendance, lms, engagement, placement, skills, feedback};
        int[] weights = {30, 15, 10, 10, 15, 10, 10};
        for (int i = 0; i < scores.length; i++) {
            if (scores[i] != null) {
                weightedTotal += scores[i] * weights[i];
                availableWeight += weights[i];
                availableCategories++;
            }
        }
        Double successScore = availableWeight == 0.0 ? null : weightedTotal / availableWeight;
        student.setSuccessScore(round(successScore));
        String overallRisk = riskFor(successScore, null, 55.0, 75.0);
        if (attendance != null && attendance < 65.0) {
            overallRisk = "High";
        }
        student.setRiskCategory(overallRisk);
        student.setDataCoveragePct(round(availableCategories * 100.0 / scores.length));
        student.setActionableInsight(buildInsight(academic, attendance, lms, engagement,
                placement, skills, feedback, availableCategories));
    }

    private static void validateInput(Student student) {
        requireRange("semester", student.getSemester(), 1.0, 8.0);
        requireRange("gpa", student.getGpa(), 0.0, 10.0);
        requireRange("attendancePct", student.getAttendancePct(), 0.0, 100.0);
        requireRange("subjectAttendanceAvg", student.getSubjectAttendanceAvg(), 0.0, 100.0);
        requireRange("lmsActivityHrs", student.getLmsActivityHrs(), 0.0, 50.0);
        requireRange("placementMockScore", student.getPlacementMockScore(), 0.0, 100.0);
        requireRange("internalAssessmentAvg", student.getInternalAssessmentAvg(), 0.0, 100.0);
        requireRange("backlogs", student.getBacklogs(), 0.0, 100.0);
        requireRange("subjectPerformanceAvg", student.getSubjectPerformanceAvg(), 0.0, 100.0);
        requireRange("lmsLoginFrequency", student.getLmsLoginFrequency(), 0.0, 100.0);
        requireRange("assignmentCompletionPct", student.getAssignmentCompletionPct(), 0.0, 100.0);
        requireRange("eventsParticipated", student.getEventsParticipated(), 0.0, 100.0);
        requireRange("clubsParticipated", student.getClubsParticipated(), 0.0, 100.0);
        requireRange("hackathonsParticipated", student.getHackathonsParticipated(), 0.0, 100.0);
        requireRange("certifications", student.getCertifications(), 0.0, 100.0);
        requireRange("aptitudeScore", student.getAptitudeScore(), 0.0, 100.0);
        requireRange("codingScore", student.getCodingScore(), 0.0, 100.0);
        requireRange("mockInterviewScore", student.getMockInterviewScore(), 0.0, 100.0);
        requireRange("technicalSkillScore", student.getTechnicalSkillScore(), 0.0, 100.0);
        requireRange("softSkillScore", student.getSoftSkillScore(), 0.0, 100.0);
        requireRange("studentSatisfactionScore", student.getStudentSatisfactionScore(), 0.0, 100.0);
        requireRange("facultyFeedbackScore", student.getFacultyFeedbackScore(), 0.0, 100.0);
    }

    private static void requireRange(String field, Number value, double minimum, double maximum) {
        if (value != null && (value.doubleValue() < minimum || value.doubleValue() > maximum)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    field + " must be between " + minimum + " and " + maximum);
        }
    }

    private static Double scale(Double value, double multiplier) {
        return value == null ? null : value * multiplier;
    }

    private static Double countScore(Integer value, int fullScoreAt) {
        return value == null ? null : Math.min(value / (double) fullScoreAt, 1.0) * 100.0;
    }

    private static Double average(Double... values) {
        double sum = 0.0;
        int count = 0;
        for (Double value : values) {
            if (value != null) {
                sum += value;
                count++;
            }
        }
        return count == 0 ? null : sum / count;
    }

    private static String riskFor(Double score, Integer backlogs, double highThreshold,
                                  double moderateThreshold) {
        if (score == null) {
            return "Insufficient data";
        }
        if (score < highThreshold || (backlogs != null && backlogs >= 2)) {
            return "High";
        }
        if (score < moderateThreshold || (backlogs != null && backlogs == 1)) {
            return "Moderate";
        }
        return "Low";
    }

    private static String academicRiskFor(Double score, Integer backlogs, Double attendance) {
        if (score == null && backlogs == null && attendance == null) {
            return "Insufficient data";
        }
        if ((score != null && score < 60.0)
                || (backlogs != null && backlogs >= 2)
                || (attendance != null && attendance < 65.0)) {
            return "High";
        }
        if ((score != null && score < 75.0)
                || (backlogs != null && backlogs == 1)
                || (attendance != null && attendance < 75.0)) {
            return "Moderate";
        }
        return "Low";
    }

    private static Double round(Double value) {
        return value == null ? null : Math.round(value * 10.0) / 10.0;
    }

    private static String buildInsight(Double academic, Double attendance, Double lms,
                                       Double engagement, Double placement, Double skills,
                                       Double feedback, int availableCategories) {
        List<String> actions = new ArrayList<>();
        if (academic != null && academic < 60.0) {
            actions.add("review subject performance, internal assessments, and backlogs");
        }
        if (attendance != null && attendance < 65.0) {
            actions.add("contact the student about attendance and agree on an attendance plan");
        }
        if (lms != null && lms < 50.0) {
            actions.add("check LMS access and assignment completion");
        }
        if (engagement != null && engagement < 40.0) {
            actions.add("recommend a relevant club, event, or certification");
        }
        if (placement != null && placement < 55.0) {
            actions.add("schedule aptitude, coding, and mock-interview practice");
        }
        if (skills != null && skills < 55.0) {
            actions.add("create a technical and soft-skills practice plan");
        }
        if (feedback != null && feedback < 50.0) {
            actions.add("follow up on student satisfaction and faculty feedback");
        }

        if (actions.isEmpty() && availableCategories < 7) {
            return "No immediate low-score signal; collect the remaining data categories "
                    + "before making a complete assessment.";
        }
        if (actions.isEmpty()) {
            return "No immediate intervention signal from the available indicators; "
                    + "continue monitoring and review the next update.";
        }
        return "Suggested follow-up: " + String.join("; ", actions) + ".";
    }
}