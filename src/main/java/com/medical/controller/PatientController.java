package com.medical.controller;

import com.medical.dto.PatientRequest;
import com.medical.entity.Patient;
import com.medical.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/createPatient")
    public ResponseEntity<?> createPatient(
            @Valid @RequestBody PatientRequest request,
            @RequestParam(defaultValue = "true") boolean verifyInsurance
    ) {
        try {
            Patient patient = patientService.createPatient(request, verifyInsurance);
            return ResponseEntity.status(HttpStatus.CREATED).body(patient);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error creating patient: " + e.getMessage());
        }
    }
    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/getPatients")
    public ResponseEntity<?> getAllPatients() {
        try {
            List<Patient> patients = patientService.getAllPatients();
            return ResponseEntity.ok(patients);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving patients: " + e.getMessage());
        }
    }
    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/getPatient")
    public ResponseEntity<?> getPatient(@RequestParam Integer patientId) {
        try {
            Patient patient = patientService.getPatient(patientId);
            return ResponseEntity.ok(patient);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving patient: " + e.getMessage());
        }
    }
    @CrossOrigin(origins = "http://localhost:3000")
    @PutMapping("/updatePatient")
    public ResponseEntity<?> updatePatient(@Valid @RequestBody Patient request) {
        try {
            Patient updatedPatient = patientService.updatePatient(request);
            return ResponseEntity.ok(updatedPatient);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error updating patient: " + e.getMessage());
        }
    }
    @CrossOrigin(origins = "http://localhost:3000")
    @DeleteMapping("/deletePatient")
    public ResponseEntity<?> deletePatient(@RequestParam Integer patientId) {
        try {
            patientService.deletePatient(patientId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error deleting patient: " + e.getMessage());
        }
    }

}