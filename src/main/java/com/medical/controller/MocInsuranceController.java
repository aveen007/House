package com.medical.controller;

import com.medical.entity.InsuranceCompany;
import com.medical.exception.ResourceNotFoundException;
import com.medical.service.MocInsuranceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.medical.dto.InsuranceCheckResponse;
import com.medical.dto.InsuranceCheckRequest;
import com.medical.repository.InsuranceCompanyRepository;

import java.util.Optional;

@RestController
@RequestMapping("/api/insurance_check")
@RequiredArgsConstructor
public class MocInsuranceController {

    private final MocInsuranceService mocInsuranceService;
    private final InsuranceCompanyRepository insuranceCompanyRepository;

    @PostMapping
    public ResponseEntity<InsuranceCheckResponse> checkInsurance(@Valid @RequestBody InsuranceCheckRequest request) {

        InsuranceCompany insuranceCompany = insuranceCompanyRepository.findById(request.getInsuranceCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Insurance company not found"));

        boolean isValid = mocInsuranceService.checkPatient(request.getFirstName(), request.getLastName(), insuranceCompany.getCompanyName());
        String message = isValid ? "Insurance is valid." : "Insurance is not valid.";
        return new ResponseEntity<>(new InsuranceCheckResponse(isValid, message), HttpStatus.OK);
    }
}

