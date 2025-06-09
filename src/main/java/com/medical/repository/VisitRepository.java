package com.medical.repository;

import com.medical.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.time.LocalDate;
import java.util.List;

public interface VisitRepository extends JpaRepository<Visit, Integer> {
    boolean existsByPatientIdAndDateOfVisit (Integer patientId, LocalDate dateOfVisit);
    List<Visit> findByPatientId(Integer patientId);

    List<Visit> findByPatientIdOrderByDateOfVisitDesc(Integer patientId);
}