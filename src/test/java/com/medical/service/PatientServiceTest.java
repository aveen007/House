package com.medical.service;

import com.medical.dto.PatientRequest;
import com.medical.entity.InsuranceCompany;
import com.medical.entity.Patient;
import com.medical.exception.InsuranceVerificationException;
import com.medical.exception.ResourceNotFoundException;
import com.medical.repository.InsuranceCompanyRepository;
import com.medical.repository.PatientRepository;
import com.medical.security.SecurityUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PatientServiceTest {

    InsuranceCompanyRepository insuranceCompanyRepository = mock(InsuranceCompanyRepository.class);
    PatientRepository patientRepository = mock(PatientRepository.class);
    InsuranceClient insuranceClient = mock(InsuranceClient.class);
    SecurityUtils securityUtils = mock(SecurityUtils.class);
    PatientService patientService = new PatientService(
            patientRepository,
            insuranceCompanyRepository,
            insuranceClient,
            securityUtils
    );

    @Test
    void createPatient_success() {
        var req = new PatientRequest();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setDateOfBirth(LocalDate.parse("1990-01-01"));
        req.setGender("M");
        req.setInsuranceCompanyId(1);

        var ic = new InsuranceCompany();
        ic.setId(1);
        ic.setCompanyName("TestInsurance");
        ic.setApiKey("k");
        ic.setApiUrl("http://example");

        when(insuranceCompanyRepository.findById(1)).thenReturn(Optional.of(ic));
        when(patientRepository.existsByFirstNameAndLastNameAndDateOfBirth(
                req.getFirstName(), req.getLastName(), req.getDateOfBirth())
        ).thenReturn(false);
        when(insuranceClient.verifyInsurance(any(), any()))
                .thenReturn(new InsuranceClient.InsuranceVerificationResponse(true, "ok"));
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> {
            Patient p = inv.getArgument(0);
            p.setId(10);
            return p;
        });

        Patient saved = patientService.createPatient(req, true);

        assertNotNull(saved.getId());
        assertEquals("John", saved.getFirstName());
        verify(insuranceClient).verifyInsurance(any(PatientRequest.class), eq(ic));
    }

    @Test
    void createPatient_unknownInsurance_throws() {
        // TC-FR01-02: Регистрация пациента с несуществующей страховой компанией
        var req = new PatientRequest();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setDateOfBirth(LocalDate.parse("1990-01-01"));
        req.setGender("M");
        req.setInsuranceCompanyId(99);
        when(insuranceCompanyRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> patientService.createPatient(req, true));
        verify(patientRepository, never()).save(any());
    }

    @Test
    void createPatient_duplicate_throws() {
        // TC-FR01-03: Регистрация пациента-дубликата (Альтернативный поток)
        var req = new PatientRequest();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setDateOfBirth(LocalDate.parse("1990-01-01"));
        req.setGender("M");
        req.setInsuranceCompanyId(1);

        var ic = new InsuranceCompany();
        ic.setId(1);
        ic.setCompanyName("TestInsurance");
        ic.setApiKey("k");
        ic.setApiUrl("http://example");

        when(insuranceCompanyRepository.findById(1)).thenReturn(Optional.of(ic));
        when(patientRepository.existsByFirstNameAndLastNameAndDateOfBirth(
                req.getFirstName(), req.getLastName(), req.getDateOfBirth())
        ).thenReturn(true); // Пациент уже существует
        // Мокируем проверку страховки, чтобы она не вызывалась (или вызывалась, но не влияла)
        when(insuranceClient.verifyInsurance(any(), any()))
                .thenReturn(new InsuranceClient.InsuranceVerificationResponse(true, "ok"));

        assertThrows(IllegalArgumentException.class, () -> patientService.createPatient(req, true));
        verify(patientRepository, never()).save(any());
    }

    @Test
    void createPatient_invalidInsuranceVerification_throws() {
        // TC-FR03-02: Ошибка проверки страховки (Альтернативный поток)
        var req = new PatientRequest();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setDateOfBirth(LocalDate.parse("1990-01-01"));
        req.setGender("M");
        req.setInsuranceCompanyId(1);

        var ic = new InsuranceCompany();
        ic.setId(1);
        ic.setCompanyName("TestInsurance");
        ic.setApiKey("k");
        ic.setApiUrl("http://example");

        when(insuranceCompanyRepository.findById(1)).thenReturn(Optional.of(ic));
        when(patientRepository.existsByFirstNameAndLastNameAndDateOfBirth(
                req.getFirstName(), req.getLastName(), req.getDateOfBirth())
        ).thenReturn(false);
        // Страховка не прошла проверку
        when(insuranceClient.verifyInsurance(any(), any()))
                .thenReturn(new InsuranceClient.InsuranceVerificationResponse(false, "Insurance verification failed"));

        assertThrows(InsuranceVerificationException.class, () -> patientService.createPatient(req, true));
        verify(patientRepository, never()).save(any());
    }
}