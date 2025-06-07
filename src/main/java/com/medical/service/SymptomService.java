package com.medical.service;

import com.medical.entity.Symptom;
import com.medical.repository.SymptomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SymptomService {

    private final SymptomRepository symptomRepository;

    public List<Symptom> getSymptoms() {
        return symptomRepository.findAll();
    }
}