package com.medical.controller;

import com.medical.dto.PatientRequest;
import com.medical.entity.Patient;
import com.medical.service.PatientService;
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
public class PatientController {

    private final PatientService patientService;
    @PostMapping("/createPatient")
    @CrossOrigin(origins = "http://localhost:3000")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<?> createPatient(
            @Valid @RequestBody PatientRequest request,
            @RequestParam(defaultValue = "true") boolean verifyInsurance
    ) {
        try {
            Patient patient = patientService.createPatient(request, verifyInsurance);
            return ResponseEntity.status(HttpStatus.CREATED).body(patient);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error creating patient: " + e.getMessage());
        }
    }
    @GetMapping("/getPatients")
    @CrossOrigin(origins = "http://localhost:3000")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','DOCTOR','HEAD_DOCTOR')")
    public ResponseEntity<?> getAllPatients() {
        try {
            List<Patient> patients = patientService.getAllPatients();
            return ResponseEntity.ok(patients);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving patients: " + e.getMessage());
        }
    }
    @GetMapping("/getPatient")
    @CrossOrigin(origins = "http://localhost:3000")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','DOCTOR','HEAD_DOCTOR','PATIENT')")
    public ResponseEntity<?> getPatient(@RequestParam Integer patientId) {
        try {
            Patient patient = patientService.getPatient(patientId);
            return ResponseEntity.ok(patient);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving patient: " + e.getMessage());
        }
    }
    @PutMapping("/updatePatient")
    @CrossOrigin(origins = "http://localhost:3000")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<?> updatePatient(@Valid @RequestBody Patient request) {
        try {
            Patient updatedPatient = patientService.updatePatient(request);
            return ResponseEntity.ok(updatedPatient);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error updating patient: " + e.getMessage());
        }
    }
    @DeleteMapping("/deletePatient")
    @CrossOrigin(origins = "http://localhost:3000")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePatient(@RequestParam Integer patientId) {
        try {
            patientService.deletePatient(patientId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error deleting patient: " + e.getMessage());
        }
    }

}