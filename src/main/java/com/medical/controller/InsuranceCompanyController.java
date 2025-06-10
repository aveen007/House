package com.medical.controller;

import com.medical.entity.InsuranceCompany;
import com.medical.entity.Patient;
import com.medical.service.InsuranceCompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequestMapping("/api/getInsuranceCompanies")
@RequiredArgsConstructor
public class InsuranceCompanyController {

    private final InsuranceCompanyService insuranceCompanyService;
    @GetMapping
    @CrossOrigin(origins = "http://localhost:3000")

    public ResponseEntity<?> getAllInsuranceCompanies() {
        try {
            List<InsuranceCompany> companies = insuranceCompanyService.getAllInsuranceCompanies();
            return ResponseEntity.ok(companies);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving insurance companies: " + e.getMessage());
        }
    }
}
