package com.smartcampus.analytics.service;

import com.smartcampus.analytics.model.Student;
import com.smartcampus.analytics.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StudentScoringBackfill implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(StudentScoringBackfill.class);

    private final StudentRepository studentRepository;
    private final ScoringEngineService scoringEngineService;

    public StudentScoringBackfill(StudentRepository studentRepository,
                                  ScoringEngineService scoringEngineService) {
        this.studentRepository = studentRepository;
        this.scoringEngineService = scoringEngineService;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<Student> studentsToUpdate = studentRepository.findAll().stream()
                .filter(student -> student.getDataCoveragePct() == null)
                .toList();

        if (studentsToUpdate.isEmpty()) {
            return;
        }

        studentsToUpdate.forEach(scoringEngineService::processStudentRiskAndScore);
        studentRepository.saveAll(studentsToUpdate);
        logger.info("Backfilled success scores and risk indicators for {} existing students",
                studentsToUpdate.size());
    }
}
