package com.medical.controller;

import com.medical.dto.BetPatientsResponse;
import com.medical.dto.BetRequest;
import com.medical.dto.VisitPatientsResponse;
import com.medical.entity.Bet;
import com.medical.entity.FinBet;
import com.medical.service.BetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BetController {

    private final BetService betService;

    @GetMapping("/getBetPatients")
    @CrossOrigin(origins = "http://localhost:3000")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','HEAD_DOCTOR')")
    public ResponseEntity<?> getBetPatients() {
        try {
            List<BetPatientsResponse> patients = betService.getAllBetPatients();
            return ResponseEntity.ok(patients);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving patients: " + e.getMessage());
        }
    }
    @GetMapping("/getVisitPatients")
    @CrossOrigin(origins = "http://localhost:3000")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','HEAD_DOCTOR')")
    public ResponseEntity<?> getVisitPatients() {
        try {
            List<VisitPatientsResponse> patients = betService.getAllVisitPatients();
            return ResponseEntity.ok(patients);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving patients: " + e.getMessage());
        }
    }

    @GetMapping("/getVisitSymptoms")
    @CrossOrigin(origins = "http://localhost:3000")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','HEAD_DOCTOR')")
    public ResponseEntity<?> getBetPatients(@RequestParam Integer visitId) {
        try {
            List<Integer> symptoms = betService.getVisitSymptoms(visitId);
            return ResponseEntity.ok(symptoms);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving patients: " + e.getMessage());
        }
    }

    @GetMapping("/getVisitStatus")
    @CrossOrigin(origins = "http://localhost:3000")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','HEAD_DOCTOR')")
    public ResponseEntity<?> getStatusForVisit(@RequestParam Integer visitId) {
        try {
            Optional<FinBet> status = betService.getStatusForVisit(visitId);
            if(status.isPresent())
                return ResponseEntity.ok(status.get());
            else
                return ResponseEntity.ok("active");

        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving patients: " + e.getMessage());
        }
    }

    @GetMapping("/getVisitBets")
    @CrossOrigin(origins = "http://localhost:3000")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','HEAD_DOCTOR')")
    public ResponseEntity<?> getBetsForVisit(@RequestParam Integer visitId) {
        try {
            List<Bet> bets = betService.getBetsForVisit(visitId);
            return ResponseEntity.ok(bets);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving patients: " + e.getMessage());
        }
    }

    @PostMapping("/createBet")
    @CrossOrigin(origins = "http://localhost:3000")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    public ResponseEntity<?> createBet(
            @Valid @RequestBody BetRequest request) {
        try {
            Bet bet = betService.createBet(request);
            return ResponseEntity.ok(bet);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving patients: " + e.getMessage());
        }
    }

    @PostMapping("/finalizeBet")
    @CrossOrigin(origins = "http://localhost:3000")
    @PreAuthorize("hasAnyRole('ADMIN','HEAD_DOCTOR')")
    public ResponseEntity<?> finalizeBet(
            @Valid @RequestBody Bet request) {
        try {
            FinBet finBet = betService.finalizeBet(request);
            return ResponseEntity.ok(finBet);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving patients: " + e.getMessage());
        }
    }
}
