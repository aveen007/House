package com.medical.service;

import com.medical.dto.PatientRequest;

import com.medical.entity.InsuranceCompany;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class InsuranceClient {

    public InsuranceVerificationResponse verifyInsurance(
            PatientRequest patientRequest,
            InsuranceCompany insuranceCompany
    ) {
        WebClient webClient = WebClient.create(insuranceCompany.getApiUrl());

        return webClient.post()
                .header("Authorization", "Bearer " + insuranceCompany.getApiKey())
                .bodyValue(new InsuranceVerificationRequest(
                        patientRequest.getFirstName(),
                        patientRequest.getLastName(),
                        patientRequest.getInsuranceCompanyId()
                ))
                .retrieve()
                .bodyToMono(InsuranceVerificationResponse.class)
                .onErrorResume(e -> Mono.just(new InsuranceVerificationResponse(false, "Verification failed")))
                .block();
    }

    public record InsuranceVerificationRequest(
            String firstName,
            String lastName,
            Integer insuranceCompanyId
    ) {}

    public record InsuranceVerificationResponse(
            boolean insured,
            String message
    ) {}
}