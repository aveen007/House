package com.medical.service;

import com.medical.dto.VisitRequest;
import com.medical.dto.VisitSymptomsRequest;
import com.medical.entity.*;
import com.medical.exception.ResourceNotFoundException;
import com.medical.repository.PatientRepository;
import com.medical.repository.SymptomRepository;
import com.medical.repository.VisitRepository;
import com.medical.repository.VisitSymptomRepository;
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
class VisitServiceTest {

    @Mock
    private VisitRepository visitRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private VisitSymptomRepository visitSymptomRepository;

    @Mock
    private SymptomRepository symptomRepository;

    @InjectMocks
    private VisitService visitService;

    private VisitRequest visitRequest;
    private Patient patient;
    private Visit visit;
    private Symptom symptom;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId(1);
        patient.setFirstName("John");
        patient.setLastName("Doe");

        visitRequest = new VisitRequest();
        visitRequest.setPatientId(1);
        visitRequest.setDateOfVisit(LocalDate.now());

        visit = new Visit();
        visit.setId(1);
        visit.setPatientId(1);
        visit.setDateOfVisit(LocalDate.now());
        visit.setHdStatus(VisitHDStatus.Awaiting);

        symptom = new Symptom();
        symptom.setId(1);
        symptom.setSymptomName("Fever");
    }

    @Test
    void testCreateVisit_Success() {
        // Given
        when(patientRepository.existsById(1)).thenReturn(true);
        when(visitRepository.existsByPatientIdAndDateOfVisit(anyInt(), any()))
                .thenReturn(false);
        when(visitRepository.save(any(Visit.class))).thenReturn(visit);

        // When
        Visit result = visitService.createVisit(visitRequest);

        // Then
        assertNotNull(result);
        assertEquals(VisitHDStatus.Awaiting, result.getHdStatus());
        verify(visitRepository, times(1)).save(any(Visit.class));
    }

    @Test
    void testCreateVisit_PatientNotFound() {
        // Given
        when(patientRepository.existsById(1)).thenReturn(false);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            visitService.createVisit(visitRequest);
        });
    }

    @Test
    void testCreateVisit_DuplicateVisit() {
        // Given
        when(patientRepository.existsById(1)).thenReturn(true);
        when(visitRepository.existsByPatientIdAndDateOfVisit(anyInt(), any()))
                .thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            visitService.createVisit(visitRequest);
        });
    }

    @Test
    void testGetAllHDAwatingVisits() {
        // Given
        List<Visit> visits = Arrays.asList(visit);
        when(visitRepository.findByHdStatusOrderByDateOfVisitDesc(VisitHDStatus.Awaiting))
                .thenReturn(visits);

        // When
        List<Visit> result = visitService.getAllHDAwatingVisits();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(visitRepository, times(1))
                .findByHdStatusOrderByDateOfVisitDesc(VisitHDStatus.Awaiting);
    }

    @Test
    void testGetAllAcceptedVisits() {
        // Given
        Visit acceptedVisit = new Visit();
        acceptedVisit.setId(2);
        acceptedVisit.setHdStatus(VisitHDStatus.Accepted);
        List<Visit> visits = Arrays.asList(acceptedVisit);
        when(visitRepository.findByHdStatusOrderByDateOfVisitDesc(VisitHDStatus.Accepted))
                .thenReturn(visits);

        // When
        List<Visit> result = visitService.getAllAcceptedVisits();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(visitRepository, times(1))
                .findByHdStatusOrderByDateOfVisitDesc(VisitHDStatus.Accepted);
    }

    @Test
    void testGetAllPatientVisits() {
        // Given
        List<Visit> visits = Arrays.asList(visit);
        when(visitRepository.findByPatientIdOrderByDateOfVisitDesc(1))
                .thenReturn(visits);

        // When
        List<Visit> result = visitService.getAllPatientVisits(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(visitRepository, times(1)).findByPatientIdOrderByDateOfVisitDesc(1);
    }

    @Test
    void testUpdateVisitHDStatus_Success() {
        // Given
        Visit updatedVisit = new Visit();
        updatedVisit.setId(1);
        updatedVisit.setHdStatus(VisitHDStatus.Accepted);

        when(visitRepository.findById(1)).thenReturn(Optional.of(visit));
        when(visitRepository.save(any(Visit.class))).thenReturn(updatedVisit);

        // When
        Visit result = visitService.updateVisitHDStatus(1, VisitHDStatus.Accepted);

        // Then
        assertNotNull(result);
        verify(visitRepository, times(1)).findById(1);
        verify(visitRepository, times(1)).save(any(Visit.class));
    }

    @Test
    void testUpdateVisitHDStatus_VisitNotFound() {
        // Given
        when(visitRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            visitService.updateVisitHDStatus(1, VisitHDStatus.Accepted);
        });
    }

    @Test
    void testAddSymptomVisit_Success() {
        // Given
        VisitSymptomsRequest request = new VisitSymptomsRequest();
        request.setSymptomIds(Arrays.asList(1, 2));

        Symptom symptom2 = new Symptom();
        symptom2.setId(2);
        symptom2.setSymptomName("Cough");

        when(visitRepository.findById(1)).thenReturn(Optional.of(visit));
        when(symptomRepository.findById(1)).thenReturn(Optional.of(symptom));
        when(symptomRepository.findById(2)).thenReturn(Optional.of(symptom2));
        when(visitSymptomRepository.save(any(VisitSymptom.class))).thenReturn(new VisitSymptom());

        // When
        visitService.addSymptomVisit(1, request);

        // Then
        verify(visitRepository, times(1)).findById(1);
        verify(symptomRepository, times(2)).findById(anyInt());
        verify(visitSymptomRepository, times(2)).save(any(VisitSymptom.class));
    }

    @Test
    void testAddSymptomVisit_VisitNotFound() {
        // Given
        VisitSymptomsRequest request = new VisitSymptomsRequest();
        request.setSymptomIds(Arrays.asList(1));

        when(visitRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            visitService.addSymptomVisit(1, request);
        });
    }

    @Test
    void testAddSymptomVisit_SymptomNotFound() {
        // Given
        VisitSymptomsRequest request = new VisitSymptomsRequest();
        request.setSymptomIds(Arrays.asList(1));

        when(visitRepository.findById(1)).thenReturn(Optional.of(visit));
        when(symptomRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            visitService.addSymptomVisit(1, request);
        });
    }
}

