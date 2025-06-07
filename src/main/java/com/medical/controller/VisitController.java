package com.medical.controller;

import com.medical.dto.VisitRequest;
import com.medical.dto.VisitSymptomsRequest;
import com.medical.entity.Patient;
import com.medical.entity.Visit;
import com.medical.service.VisitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/visits")
@RequiredArgsConstructor
public class VisitController {

    private final VisitService visitService;
    @PostMapping
    public ResponseEntity<?> createVisit(
            @Valid @RequestBody VisitRequest request
    ) {
        try {
            Visit visit = visitService.createVisit(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(visit);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error creating patient: " + e.getMessage());
        }
    }

    @PostMapping(":{visitId}/symptoms")
    public ResponseEntity<?> addSymptomsVisit(
            @PathVariable Integer visitId,
            @Valid @RequestBody VisitSymptomsRequest request
    ) {
        try {
            visitService.addSymptomVisit(visitId, request);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error creating patient: " + e.getMessage());
        }
    }
}
