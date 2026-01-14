package com.medical.service;

import com.medical.dto.PatientRequest;
import com.medical.entity.InsuranceCompany;
import com.medical.entity.Patient;
import com.medical.exception.InsuranceVerificationException;
import com.medical.exception.ResourceNotFoundException;
import com.medical.repository.InsuranceCompanyRepository;
import com.medical.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private InsuranceCompanyRepository insuranceCompanyRepository;

    @Mock
    private InsuranceClient insuranceClient;

    @InjectMocks
    private PatientService patientService;

    private InsuranceCompany insuranceCompany;
    private PatientRequest patientRequest;
    private Patient patient;

    @BeforeEach
    void setUp() {
        insuranceCompany = new InsuranceCompany();
        insuranceCompany.setId(1);
        insuranceCompany.setCompanyName("Test Insurance");
        insuranceCompany.setApiUrl("http://test-api.com");
        insuranceCompany.setApiKey("test-key");

        patientRequest = new PatientRequest();
        patientRequest.setFirstName("John");
        patientRequest.setLastName("Doe");
        patientRequest.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patientRequest.setGender("M");
        patientRequest.setInsuranceCompanyId(1);

        patient = new Patient();
        patient.setId(1);
        patient.setFirstName("John");
        patient.setLastName("Doe");
        patient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patient.setGender("M");
        patient.setInsuranceCompany(insuranceCompany);
    }

    @Test
    void testCreatePatient_Success() {
        // Given
        when(insuranceCompanyRepository.findById(1)).thenReturn(Optional.of(insuranceCompany));
        when(insuranceClient.verifyInsurance(any(), any()))
                .thenReturn(new InsuranceClient.InsuranceVerificationResponse(true, "Verified"));
        when(patientRepository.existsByFirstNameAndLastNameAndDateOfBirth(anyString(), anyString(), any()))
                .thenReturn(false);
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        // When
        Patient result = patientService.createPatient(patientRequest, true);

        // Then
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    void testCreatePatient_InsuranceCompanyNotFound() {
        // Given
        when(insuranceCompanyRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            patientService.createPatient(patientRequest, true);
        });
    }

    @Test
    void testCreatePatient_InsuranceVerificationFailed() {
        // Given
        when(insuranceCompanyRepository.findById(1)).thenReturn(Optional.of(insuranceCompany));
        when(insuranceClient.verifyInsurance(any(), any()))
                .thenReturn(new InsuranceClient.InsuranceVerificationResponse(false, "Not insured"));

        // When & Then
        assertThrows(InsuranceVerificationException.class, () -> {
            patientService.createPatient(patientRequest, true);
        });
    }

    @Test
    void testCreatePatient_DuplicatePatient() {
        // Given
        when(insuranceCompanyRepository.findById(1)).thenReturn(Optional.of(insuranceCompany));
        when(insuranceClient.verifyInsurance(any(), any()))
                .thenReturn(new InsuranceClient.InsuranceVerificationResponse(true, "Verified"));
        when(patientRepository.existsByFirstNameAndLastNameAndDateOfBirth(anyString(), anyString(), any()))
                .thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            patientService.createPatient(patientRequest, true);
        });
    }

    @Test
    void testCreatePatient_FutureDateOfBirth() {
        // Given
        patientRequest.setDateOfBirth(LocalDate.now().plusDays(1));
        when(insuranceCompanyRepository.findById(1)).thenReturn(Optional.of(insuranceCompany));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            patientService.createPatient(patientRequest, true);
        });
    }

    @Test
    void testGetAllPatients() {
        // Given
        List<Patient> patients = Arrays.asList(patient);
        when(patientRepository.findAll()).thenReturn(patients);

        // When
        List<Patient> result = patientService.getAllPatients();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(patientRepository, times(1)).findAll();
    }

    @Test
    void testGetPatient_Success() {
        // Given
        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));

        // When
        Patient result = patientService.getPatient(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(patientRepository, times(1)).findById(1);
    }

    @Test
    void testGetPatient_NotFound() {
        // Given
        when(patientRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            patientService.getPatient(1);
        });
    }

    @Test
    void testUpdatePatient_Success() {
        // Given
        Patient updatedPatient = new Patient();
        updatedPatient.setId(1);
        updatedPatient.setFirstName("Jane");
        updatedPatient.setLastName("Doe");
        updatedPatient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        updatedPatient.setGender("F");
        updatedPatient.setInsuranceCompany(insuranceCompany);

        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));
        when(insuranceCompanyRepository.findById(1)).thenReturn(Optional.of(insuranceCompany));
        when(patientRepository.save(any(Patient.class))).thenReturn(updatedPatient);

        // When
        Patient result = patientService.updatePatient(updatedPatient);

        // Then
        assertNotNull(result);
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    void testDeletePatient_Success() {
        // Given
        when(patientRepository.existsById(1)).thenReturn(true);
        doNothing().when(patientRepository).deleteById(1);

        // When
        patientService.deletePatient(1);

        // Then
        verify(patientRepository, times(1)).deleteById(1);
    }

    @Test
    void testDeletePatient_NotFound() {
        // Given
        when(patientRepository.existsById(1)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            patientService.deletePatient(1);
        });
    }
}

