package com.medical.controller;

import com.medical.contracts.TermsAndConditionsEntity;
import com.medical.dto.ContractCreateRequest;
import com.medical.dto.ContractResponse;
import com.medical.dto.ContractSaveRequest;
import com.medical.dto.ContractSignRequest;
import com.medical.service.ContractService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@CrossOrigin(origins = "http://localhost:3000")
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @GetMapping("/terms")
    @PreAuthorize("isAuthenticated()")
    public List<TermsAndConditionsEntity> getAllTerms() {
        return contractService.getAllTerms();
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','DOCTOR','HEAD_DOCTOR','PATIENT','LAWYER')")
    public List<ContractResponse> getPatientContracts(@PathVariable Integer patientId) {
        return contractService.getPatientContracts(patientId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','LAWYER','PATIENT')")
    public ContractResponse createContract(@RequestBody ContractCreateRequest req) {
        return contractService.createContract(req);
    }

    @PutMapping("/{contractId}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','LAWYER','PATIENT')")
    public ContractResponse saveContract(@PathVariable Integer contractId, @RequestBody ContractSaveRequest req) {
        return contractService.saveContract(contractId, req);
    }

    @GetMapping("/{contractId}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','DOCTOR','HEAD_DOCTOR','PATIENT','LAWYER')")
    public ContractResponse viewContract(@PathVariable Integer contractId) {
        return contractService.viewContract(contractId);
    }

    @PostMapping("/{contractId}/sign")
    @PreAuthorize("hasAnyRole('ADMIN','PATIENT')")
    public ContractResponse signContract(@PathVariable Integer contractId, @RequestBody ContractSignRequest req) {
        return contractService.signContract(contractId, req);
    }
}
