package com.medical.repository;

import com.medical.entity.VisitSymptom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

public interface VisitSymptomRepository extends JpaRepository<VisitSymptom, Integer> {
    List<VisitSymptom> findByVisitId(Integer visitId);
}