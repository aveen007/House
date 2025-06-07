package com.medical.service;

import com.medical.dto.PatientRequest;
import com.medical.entity.InsuranceCompany;
import com.medical.entity.Patient;
import com.medical.exception.InsuranceVerificationException;
import com.medical.exception.ResourceNotFoundException;
import com.medical.repository.PatientInsuranceRepository;
import com.medical.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;

@Service
@RequiredArgsConstructor
public class MocInsuranceService {

    private final PatientInsuranceRepository patientInsuranceRepository;

    @Transactional
    public boolean checkPatient(String firstName, String lastName, String companyName) {
        // Check if the patient insurance record exists
        return patientInsuranceRepository.existsByPatientFirstNameAndPatientLastNameAndCompanyName(firstName, lastName, companyName);
    }
}

