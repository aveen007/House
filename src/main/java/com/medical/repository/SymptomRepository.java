package com.medical.repository;

import com.medical.entity.Symptom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.time.LocalDate;

public interface SymptomRepository extends JpaRepository<Symptom, Integer> {
}