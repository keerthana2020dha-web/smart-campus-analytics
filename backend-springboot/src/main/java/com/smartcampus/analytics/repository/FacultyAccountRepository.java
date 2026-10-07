package com.smartcampus.analytics.repository;

import com.smartcampus.analytics.model.FacultyAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FacultyAccountRepository extends JpaRepository<FacultyAccount, Long> {
    Optional<FacultyAccount> findByFacultyId(String facultyId);
}
