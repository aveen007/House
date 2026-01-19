package com.medical.controller;

import com.medical.dto.AnalysisResultRequest;
import com.medical.dto.PatientAnalysisRequest;
import com.medical.entity.Analysis;
import com.medical.entity.AnalysisStatus;
import com.medical.entity.PatientAnalysis;
import com.medical.service.AnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/getAnalysesTypes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAnalyses() {
        try {
            List<Analysis> analyses = analysisService.getAllAnalysis();
            return ResponseEntity.ok(analyses);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving analyses: " + e.getMessage());
        }
    }



    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/createAnalysisResult")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<?> createAnalysisResult(
            @Valid @RequestBody AnalysisResultRequest request
    ) {
        try {
            var analysisResult = analysisService.createAnalysisResult(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(analysisResult);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error creating analysis result: " + e.getMessage());
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/createPatientAnalysis")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    public ResponseEntity<?> createPatientAnalysis(
            @Valid @RequestBody PatientAnalysisRequest request,
            @RequestParam(defaultValue = "true") boolean verifyInsurance
    ) {
        try {
            PatientAnalysis patientAnalysis = analysisService.createPatientAnalysis(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(patientAnalysis);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error creating patient analysis: " + e.getMessage());
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/getPatientAnalyses")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','HEAD_DOCTOR','PATIENT')")
    public ResponseEntity<?> getPatientAnalyses(
            @RequestParam Integer patientId) {
        try {
            var patientAnalysis = analysisService.getPatientAnalyses(patientId);
            return ResponseEntity.status(HttpStatus.CREATED).body(patientAnalysis);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error getting analyses: " + e.getMessage());
        }
    }



    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/getAwaitingHDAnalyses")
    @PreAuthorize("hasAnyRole('ADMIN','HEAD_DOCTOR')")
    public ResponseEntity<?> getAwaitingAnalyses() {
        try {
            var patientsAnalyses = analysisService.getAwaitingHDAnalyses();
            return ResponseEntity.status(HttpStatus.CREATED).body(patientsAnalyses);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error getting analyses: " + e.getMessage());
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/getApprovedAnalyses")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','HEAD_DOCTOR')")
    public ResponseEntity<?> getApprovedAnalyses() {
        try {
            var patientsAnalyses = analysisService.getApprovedAnalyses();
            return ResponseEntity.status(HttpStatus.CREATED).body(patientsAnalyses);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error getting analyses: " + e.getMessage());
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/getBetAnalyses")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','HEAD_DOCTOR')")
    public ResponseEntity<?> getBetAnalyses(
            @RequestParam Integer betId) {
        try {
            var betAnalysis = analysisService.getBetAnalyses(betId);
            return ResponseEntity.status(HttpStatus.CREATED).body(betAnalysis);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error getting analyses: " + e.getMessage());
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PutMapping("/updatePatientAnalysisStatus")
    @PreAuthorize("hasAnyRole('ADMIN','HEAD_DOCTOR')")
    public ResponseEntity<?> updatePatientAnalysisStatus(
            @RequestParam Integer patientAnalysisId,
            @Valid @RequestBody AnalysisStatus status) {
        try {
            PatientAnalysis patientAnalysis =
                    analysisService.updatePatientAnalysisStatus(patientAnalysisId, status);
            return ResponseEntity.status(HttpStatus.CREATED).body(patientAnalysis);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error updating status: " + e.getMessage());
        }
    }
    @CrossOrigin(origins = "http://localhost:3000")

    @GetMapping("/patientAnalyses")
    @PreAuthorize("hasAnyRole('ADMIN','HEAD_DOCTOR')")
    public ResponseEntity<List<PatientAnalysis>> getAllPatientAnalyses() {
        return ResponseEntity.ok(analysisService.getAllPatientAnalyses());
    }

}
