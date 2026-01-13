package com.medical.service;

import com.medical.contracts.*;
import com.medical.dto.*;
import com.medical.exception.ResourceNotFoundException;
import com.medical.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ContractService {

    private final ContractRepository contractRepository;
    private final TermsAndConditionsRepository termsRepository;
    private final PatientRepository patientRepository;

    public ContractService(
            ContractRepository contractRepository,
            TermsAndConditionsRepository termsRepository,
            PatientRepository patientRepository
    ) {
        this.contractRepository = contractRepository;
        this.termsRepository = termsRepository;
        this.patientRepository = patientRepository;
    }

    @Transactional(readOnly = true)
    public List<TermsAndConditionsEntity> getAllTerms() {
        return termsRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ContractResponse> getPatientContracts(Integer patientId) {
        patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + patientId));
        
        var contracts = contractRepository.findAllByPatientId(patientId);
        return contracts.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ContractResponse createContract(ContractCreateRequest req) {
        patientRepository.findById(req.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + req.getPatientId()));

        var terms = termsRepository.findById(req.getTermsId())
                .orElseThrow(() -> new ResourceNotFoundException("Terms not found: " + req.getTermsId()));

        var c = new ContractEntity();
        c.setPatientId(req.getPatientId());
        c.setTerms(terms);

        c.setTermsSnapshot(terms.getContent());

        c.setStatus(ContractStatus.DRAFT);
        c.setCreatedAt(OffsetDateTime.now());
        c.setUpdatedAt(OffsetDateTime.now());

        var saved = contractRepository.save(c);
        return toResponse(saved);
    }

    @Transactional
    public ContractResponse saveContract(Integer contractId, ContractSaveRequest req) {
        var c = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found: " + contractId));

        if (c.getStatus() == ContractStatus.SIGNED || c.getStatus() == ContractStatus.REVOKED) {
            throw new IllegalStateException("Contract is not editable in status: " + c.getStatus());
        }

        if (req.getTermsId() != null) {
            if (c.getStatus() != ContractStatus.DRAFT) {
                throw new IllegalStateException("Terms can be changed only in DRAFT status");
            }
            var terms = termsRepository.findById(req.getTermsId())
                    .orElseThrow(() -> new ResourceNotFoundException("Terms not found: " + req.getTermsId()));
            c.setTerms(terms);
            c.setTermsSnapshot(terms.getContent());
        }

        if (req.getStatus() != null && !req.getStatus().isBlank()) {
            var newStatus = ContractStatus.valueOf(req.getStatus());
            if (newStatus != ContractStatus.DRAFT && newStatus != ContractStatus.READY) {
                throw new IllegalArgumentException("Only DRAFT or READY allowed for saveContract");
            }
            c.setStatus(newStatus);
        }

        c.setUpdatedAt(OffsetDateTime.now());
        var saved = contractRepository.save(c);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ContractResponse viewContract(Integer contractId) {
        var c = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found: " + contractId));
        return toResponse(c);
    }

    @Transactional
    public ContractResponse signContract(Integer contractId, ContractSignRequest req) {
        var c = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found: " + contractId));

        if (!c.getPatientId().equals(req.getPatientId())) {
            throw new IllegalStateException("Contract belongs to another patient");
        }

        if (c.getStatus() != ContractStatus.READY) {
            throw new IllegalStateException("Contract must be READY to sign. Current: " + c.getStatus());
        }

        c.setStatus(ContractStatus.SIGNED);
        c.setSignedAt(OffsetDateTime.now());
        c.setUpdatedAt(OffsetDateTime.now());
        c.setSignedBy(req.getSignedBy());
        c.setSignature(req.getSignature());

        var saved = contractRepository.save(c);
        return toResponse(saved);
    }

    private ContractResponse toResponse(ContractEntity c) {
        var t = c.getTerms();

        var resp = new ContractResponse();
        resp.setContractId(c.getContractId());
        resp.setPatientId(c.getPatientId());

        resp.setStatus(c.getStatus().name());
        resp.setCreatedAt(c.getCreatedAt());
        resp.setUpdatedAt(c.getUpdatedAt());
        resp.setSignedAt(c.getSignedAt());

        resp.setSignedBy(c.getSignedBy());
        resp.setSignature(c.getSignature());

        resp.setTermsId(t.getTermsId());
        resp.setTermsVersion(t.getVersion());
        resp.setTermsTitle(t.getTitle());

        resp.setTermsSnapshot(c.getTermsSnapshot());
        return resp;
    }
}
