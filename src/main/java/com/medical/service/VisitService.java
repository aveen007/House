package com.medical.service;

import com.medical.dto.VisitRequest;
import com.medical.dto.VisitSymptomsRequest;
import com.medical.entity.*;
import com.medical.exception.InsuranceVerificationException;
import com.medical.exception.ResourceNotFoundException;
import com.medical.repository.PatientRepository;
import com.medical.repository.SymptomRepository;
import com.medical.repository.VisitRepository;
import com.medical.repository.VisitSymptomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VisitService {

    private final VisitRepository visitRepository;
    private final PatientRepository patientRepository;
    private final VisitSymptomRepository visitSymptomRepository;
    private final SymptomRepository symptomRepository;

    @Transactional
    public Visit createVisit(VisitRequest request) {
        // Валидация данных (точка расширения)
        validateVisitData(request);

        if(visitRepository.existsByPatientIdAndDateOfVisit(
                request.getPatientId(), request.getDateOfVisit())
        )
        {
            throw new IllegalArgumentException("A visit with the same patient and date already exists.");
        }
        Visit visit = new Visit();
        visit.setPatientId(request.getPatientId());
        visit.setDateOfVisit(request.getDateOfVisit());

        return visitRepository.save(visit);
    }

    @Transactional
    public void addSymptomVisit(Integer visitId, VisitSymptomsRequest request) {

        Visit existingVisit = visitRepository.findById(visitId)
                .orElseThrow(() -> new ResourceNotFoundException("Visit not found"));

        for (Integer symptomId : request.getSymptomIds()) {
            VisitSymptom visitSymptom = new VisitSymptom();

            visitSymptom.setVisit(existingVisit);

            Symptom existingSymptom = symptomRepository.findById(symptomId)
                    .orElseThrow(() -> new ResourceNotFoundException("Symptom not found"));

            visitSymptom.setSymptom(existingSymptom);

            visitSymptomRepository.save(visitSymptom);
        }
    }

    private void validateVisitData(VisitRequest request)
    {
        if(!patientRepository.existsById(request.getPatientId()))
        {
            throw new IllegalArgumentException("Patient with that id doesn't exist");
        }
    }
}
