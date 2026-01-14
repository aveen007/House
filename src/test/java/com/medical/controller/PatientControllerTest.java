package com.medical.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medical.dto.PatientRequest;
import com.medical.entity.InsuranceCompany;
import com.medical.entity.Patient;
import com.medical.repository.InsuranceCompanyRepository;
import com.medical.repository.PatientRepository;
import com.medical.service.InsuranceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private InsuranceCompanyRepository insuranceCompanyRepository;

    @MockBean
    private InsuranceClient insuranceClient;

    private InsuranceCompany insuranceCompany;

    @BeforeEach
    void setUp() {
        patientRepository.deleteAll();
        insuranceCompanyRepository.deleteAll();

        insuranceCompany = new InsuranceCompany();
        insuranceCompany.setCompanyName("Test Insurance");
        insuranceCompany.setApiUrl("http://test-api.com");
        insuranceCompany.setApiKey("test-key");
        insuranceCompany = insuranceCompanyRepository.save(insuranceCompany);
    }

    @Test
    void testCreatePatient_Success() throws Exception {
        PatientRequest request = new PatientRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));
        request.setGender("M");
        request.setInsuranceCompanyId(insuranceCompany.getId());

        when(insuranceClient.verifyInsurance(any(), any()))
                .thenReturn(new InsuranceClient.InsuranceVerificationResponse(true, "Verified"));

        mockMvc.perform(post("/api/createPatient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void testCreatePatient_InvalidData() throws Exception {
        PatientRequest request = new PatientRequest();
        request.setFirstName(""); // Invalid: empty first name
        request.setLastName("Doe");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));
        request.setGender("M");
        request.setInsuranceCompanyId(insuranceCompany.getId());

        mockMvc.perform(post("/api/createPatient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllPatients() throws Exception {
        // Create a test patient
        Patient patient = new Patient();
        patient.setFirstName("John");
        patient.setLastName("Doe");
        patient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patient.setGender("M");
        patient.setInsuranceCompany(insuranceCompany);
        patientRepository.save(patient);

        mockMvc.perform(get("/api/getPatients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("John"));
    }

    @Test
    void testGetPatient_Success() throws Exception {
        Patient patient = new Patient();
        patient.setFirstName("John");
        patient.setLastName("Doe");
        patient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patient.setGender("M");
        patient.setInsuranceCompany(insuranceCompany);
        patient = patientRepository.save(patient);

        mockMvc.perform(get("/api/getPatient")
                        .param("patientId", patient.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(patient.getId()))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void testGetPatient_NotFound() throws Exception {
        mockMvc.perform(get("/api/getPatient")
                        .param("patientId", "999"))
                .andExpect(status().isInternalServerError());
    }
}

