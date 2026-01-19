package com.medical.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medical.dto.PatientRequest;
import com.medical.entity.Patient;
import com.medical.service.PatientService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientController.class)
@AutoConfigureMockMvc(addFilters = false)
class PatientControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper om;

    @MockBean
    PatientService patientService;

    @Test
    void createPatient_returns201() throws Exception {
        var req = new PatientRequest();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setDateOfBirth(LocalDate.parse("1990-01-01"));
        req.setGender("M");
        req.setInsuranceCompanyId(1);

        Patient saved = new Patient();
        saved.setId(1);
        saved.setFirstName("John");
        saved.setLastName("Doe");

        Mockito.when(patientService.createPatient(Mockito.any(), Mockito.anyBoolean()))
                .thenReturn(saved);

        mvc.perform(post("/api/createPatient")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getPatients_returnsList() throws Exception {
        Patient p = new Patient();
        p.setId(1);
        p.setFirstName("John");
        p.setLastName("Doe");

        Mockito.when(patientService.getAllPatients()).thenReturn(List.of(p));

        mvc.perform(get("/api/getPatients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }
}

