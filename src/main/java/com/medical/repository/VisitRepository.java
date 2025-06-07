package com.medical.repository;

import com.medical.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.time.LocalDate;

public interface VisitRepository extends JpaRepository<Visit, Integer> {
    boolean existsByPatientIdAndDateOfVisit (Integer patientId, LocalDate dateOfVisit);
}