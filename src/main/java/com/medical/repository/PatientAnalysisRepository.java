package com.medical.repository;

import com.medical.entity.AnalysisStatus;
import com.medical.entity.PatientAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientAnalysisRepository extends JpaRepository<PatientAnalysis, Integer> {
    List<PatientAnalysis> findByPatientId(Integer patientId);

    List<PatientAnalysis> findByStatus(AnalysisStatus status);
}
