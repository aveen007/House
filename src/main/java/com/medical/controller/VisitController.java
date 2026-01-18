package com.medical.controller;

import com.medical.dto.VisitRequest;
import com.medical.dto.VisitSymptomsRequest;
import com.medical.entity.Patient;
import com.medical.entity.Visit;
import com.medical.entity.VisitHDStatus;
import com.medical.service.VisitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/visits")
@RequiredArgsConstructor
public class VisitController {

    private final VisitService visitService;
    @PostMapping
    @CrossOrigin(origins = "http://localhost:3000")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<?> createVisit(@Valid @RequestBody VisitRequest request) {
        try {
            Visit visit = visitService.createVisit(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(visit);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error creating patient: " + e.getMessage());
        }
    }

    @PutMapping("{visitId}/updateHDStatus")
    @CrossOrigin(origins = "http://localhost:3000")
    @PreAuthorize("hasRole('HEAD_DOCTOR')")
    public ResponseEntity<?> updateVisitHDStatus(
            @PathVariable Integer visitId,
            @Valid @RequestBody VisitHDStatus new_status
    ) {
        try {
            Visit updatedVisit = visitService.updateVisitHDStatus(visitId, new_status);
            return ResponseEntity.ok(updatedVisit);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error updating visit: " + e.getMessage());
        }
    }

    @GetMapping("/getAllAcceptedVisits")
    @CrossOrigin(origins = "http://localhost:3000")
    @PreAuthorize("hasAnyRole('DOCTOR','HEAD_DOCTOR')")
    public ResponseEntity<?> getAllAcceptedVisits(){
        try {
            var visits = visitService.getAllAcceptedVisits();
            return ResponseEntity.ok(visits);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error getting visits: " + e.getMessage());
        }
    }

    @GetMapping("/getAllHDAwaitingVisits")
    @CrossOrigin(origins = "http://localhost:3000")
    @PreAuthorize("hasRole('HEAD_DOCTOR')")
    public ResponseEntity<?> getAllHDAwatingVisits(){
        try {
                var visits = visitService.getAllHDAwatingVisits();
                return ResponseEntity.ok(visits);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Error getting visits: " + e.getMessage());
            }
    }

    @GetMapping("/getAllPatientVisits")
    @CrossOrigin(origins = "http://localhost:3000")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','DOCTOR','HEAD_DOCTOR','PATIENT')")
    public ResponseEntity<?> getAllPatientVisits(@RequestParam Integer patientId){
        try {
            var visits = visitService.getAllPatientVisits(patientId);
            return ResponseEntity.ok(visits);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error getting visits: " + e.getMessage());
        }
    }


    @PostMapping("{visitId}/symptoms")
    @CrossOrigin(origins = "http://localhost:3000")
    @PreAuthorize("hasRole('DOCTOR')")
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
