package com.medical.service;

import com.medical.dto.VisitRequest;
import com.medical.entity.Visit;
import com.medical.entity.VisitHDStatus;
import com.medical.repository.PatientRepository;
import com.medical.repository.SymptomRepository;
import com.medical.repository.VisitRepository;
import com.medical.repository.VisitSymptomRepository;
import com.medical.security.SecurityUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class VisitServiceTest {

    VisitRepository visitRepository = mock(VisitRepository.class);
    PatientRepository patientRepository = mock(PatientRepository.class);
    VisitSymptomRepository visitSymptomRepository = mock(VisitSymptomRepository.class);
    SymptomRepository symptomRepository = mock(SymptomRepository.class);
    SecurityUtils securityUtils = mock(SecurityUtils.class);

    VisitService visitService = new VisitService(
            visitRepository,
            patientRepository,
            visitSymptomRepository,
            symptomRepository,
            securityUtils
    );

    @Test
    void createVisit_setsAwaitingStatus() {
        var request = new VisitRequest();
        request.setPatientId(1);
        request.setDateOfVisit(LocalDate.now());

        when(patientRepository.existsById(1)).thenReturn(true);
        when(visitRepository.existsByPatientIdAndDateOfVisit(1, request.getDateOfVisit()))
                .thenReturn(false);
        when(visitRepository.save(any(Visit.class))).thenAnswer(inv -> {
            Visit v = inv.getArgument(0);
            v.setId(10);
            return v;
        });

        Visit saved = visitService.createVisit(request);

        assertNotNull(saved.getId());
        assertEquals(VisitHDStatus.Awaiting, saved.getHdStatus());
    }

    @Test
    void updateVisitStatus_changesStatus() {
        Visit v = new Visit();
        v.setId(5);
        v.setHdStatus(VisitHDStatus.Awaiting);
        when(visitRepository.findById(5)).thenReturn(Optional.of(v));
        when(visitRepository.save(any(Visit.class))).thenAnswer(inv -> inv.getArgument(0));

        Visit updated = visitService.updateVisitHDStatus(5, VisitHDStatus.Accepted);

        assertEquals(VisitHDStatus.Accepted, updated.getHdStatus());
    }
}

