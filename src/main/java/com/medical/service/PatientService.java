package com.medical.service;

import com.medical.dto.PatientRequest;
import com.medical.entity.InsuranceCompany;
import com.medical.entity.Patient;
import com.medical.exception.InsuranceVerificationException;
import com.medical.exception.ResourceNotFoundException;
import com.medical.repository.InsuranceCompanyRepository;
import com.medical.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final InsuranceCompanyRepository insuranceCompanyRepository;
    private final InsuranceClient insuranceClient;

    @Transactional
    public Patient createPatient(PatientRequest request, boolean verifyInsurance) {
        // Валидация данных (точка расширения)
        validatePatientData(request);

        // Получение страховой компании
        InsuranceCompany insuranceCompany = insuranceCompanyRepository.findById(request.getInsuranceCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Insurance company not found"));

        // Проверка страховки
        if (verifyInsurance) {
            InsuranceClient.InsuranceVerificationResponse response =
                    insuranceClient.verifyInsurance(request, insuranceCompany);

            if (!response.insured()) {
                throw new InsuranceVerificationException(
                        "Insurance verification failed: " + response.message()
                );
            }
        }

        if(patientRepository.existsByFirstNameAndLastNameAndDateOfBirth(
                request.getFirstName(), request.getLastName(), request.getDateOfBirth())
        )
        {
            throw new IllegalArgumentException("A patient with the same name and date of birth already exists.");
        }
        // Создание пациента
        Patient patient = new Patient();
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
//        patient.setAge(calculateAge(request.getDateOfBirth()));
        patient.setInsuranceCompany(insuranceCompany);

        return patientRepository.save(patient);
    }

    private int calculateAge(LocalDate dateOfBirth) {
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    private void validatePatientData(PatientRequest request) {
        if (request.getDateOfBirth().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date of birth cannot be in the future");
        }
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public Patient getPatient(Integer patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
    }

    @Transactional
    public Patient updatePatient(Patient patient) {
        PatientRequest request = new PatientRequest();

        request.setFirstName(patient.getFirstName());
        request.setLastName(patient.getLastName());
        request.setGender(patient.getGender());
        request.setInsuranceCompanyId(patient.getInsuranceCompany().getId());
        request.setDateOfBirth(patient.getDateOfBirth());

        // Validate the incoming data
        validatePatientData(request);

        // Find the existing patient
        Patient existingPatient = patientRepository.findById(patient.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        InsuranceCompany insuranceCompany = insuranceCompanyRepository.findById(request.getInsuranceCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Insurance company not found"));

        // Update the patient's details
        existingPatient.setFirstName(request.getFirstName());
        existingPatient.setLastName(request.getLastName());
        existingPatient.setDateOfBirth(request.getDateOfBirth());
        existingPatient.setGender(request.getGender());
        existingPatient.setInsuranceCompany(insuranceCompany);
        // Save the updated patient
        return patientRepository.save(existingPatient);
    }

    @Transactional
    public void deletePatient(Integer patientId) {
        // Check if the patient exists
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found");
        }
        // Delete the patient
        patientRepository.deleteById(patientId);
    }
}