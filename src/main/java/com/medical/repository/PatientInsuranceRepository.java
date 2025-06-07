package com.medical.repository;

import com.medical.entity.PatientInsurance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientInsuranceRepository extends JpaRepository<PatientInsurance, Integer> {
    boolean existsByPatientFirstNameAndPatientLastNameAndCompanyName(String firstName, String lastName, String companyName);
}
