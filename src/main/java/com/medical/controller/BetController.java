package com.medical.controller;

import com.medical.dto.BetPatientsResponse;
import com.medical.dto.BetRequest;
import com.medical.dto.PatientRequest;
import com.medical.entity.Bet;
import com.medical.entity.FinBet;
import com.medical.entity.Patient;
import com.medical.repository.PatientRepository;
import com.medical.repository.VisitRepository;
import com.medical.service.BetService;
import com.medical.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BetController {

    private final BetService betService;

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/getBetPatients")
    public ResponseEntity<?> getBetPatients() {
        try {
            List<BetPatientsResponse> patients = betService.getAllBetPatients();
            return ResponseEntity.ok(patients);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving patients: " + e.getMessage());
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/getVisitSymptoms")
    public ResponseEntity<?> getBetPatients(@RequestParam Integer visitId) {
        try {
            List<Integer> symptoms = betService.getVisitSymptoms(visitId);
            return ResponseEntity.ok(symptoms);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving patients: " + e.getMessage());
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/getVisitStatus")
    public ResponseEntity<?> getStatusForVisit(@RequestParam Integer visitId) {
        try {
            Optional<FinBet> status = betService.getStatusForVisit(visitId);
            if(status.isPresent())
                return ResponseEntity.ok(status.get());
            else
                return ResponseEntity.ok("active");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving patients: " + e.getMessage());
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/getVisitBets")
    public ResponseEntity<?> getBetsForVisit(@RequestParam Integer visitId) {
        try {
            List<Bet> bets = betService.getBetsForVisit(visitId);
            return ResponseEntity.ok(bets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving patients: " + e.getMessage());
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/createBet")
    public ResponseEntity<?> createBet(
            @Valid @RequestBody BetRequest request) {
        try {
            Bet bet = betService.createBet(request);
            return ResponseEntity.ok(bet);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving patients: " + e.getMessage());
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/finalizeBet")
    public ResponseEntity<?> finalizeBet(
            @Valid @RequestBody Bet request) {
        try {
            FinBet finBet = betService.finalizeBet(request);
            return ResponseEntity.ok(finBet);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving patients: " + e.getMessage());
        }
    }
}
