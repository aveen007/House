package com.medical.controller;

import com.medical.entity.Symptom;
import com.medical.service.SymptomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SymptomController {

    private final SymptomService symptomService;

    @GetMapping("/getSymptoms")
    public ResponseEntity<List<Symptom>> getAllSymptoms() {
        List<Symptom> symptoms = symptomService.getSymptoms();
        return ResponseEntity.ok(symptoms);
    }
}
