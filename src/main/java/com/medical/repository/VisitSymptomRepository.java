package com.medical.repository;

import com.medical.entity.VisitSymptom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.time.LocalDate;

public interface VisitSymptomRepository extends JpaRepository<VisitSymptom, Integer> {
}