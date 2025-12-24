package com.medical.controller;

import com.medical.contracts.TermsAndConditionsEntity;
import com.medical.dto.ContractCreateRequest;
import com.medical.dto.ContractResponse;
import com.medical.dto.ContractSaveRequest;
import com.medical.dto.ContractSignRequest;
import com.medical.service.ContractService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @GetMapping("/terms")
    public List<TermsAndConditionsEntity> getAllTerms() {
        return contractService.getAllTerms();
    }

    @GetMapping("/patient/{patientId}")
    public List<ContractResponse> getPatientContracts(@PathVariable Integer patientId) {
        return contractService.getPatientContracts(patientId);
    }

    @PostMapping
    public ContractResponse createContract(@RequestBody ContractCreateRequest req) {
        return contractService.createContract(req);
    }

    @PutMapping("/{contractId}")
    public ContractResponse saveContract(@PathVariable Integer contractId, @RequestBody ContractSaveRequest req) {
        return contractService.saveContract(contractId, req);
    }

    @GetMapping("/{contractId}")
    public ContractResponse viewContract(@PathVariable Integer contractId) {
        return contractService.viewContract(contractId);
    }

    @PostMapping("/{contractId}/sign")
    public ContractResponse signContract(@PathVariable Integer contractId, @RequestBody ContractSignRequest req) {
        return contractService.signContract(contractId, req);
    }
}
