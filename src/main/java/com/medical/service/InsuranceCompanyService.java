package com.medical.service;

import com.medical.entity.InsuranceCompany;
import com.medical.repository.InsuranceCompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InsuranceCompanyService {

    private final InsuranceCompanyRepository insuranceCompanyRepository;

    public List<InsuranceCompany> getAllInsuranceCompanies() {
        return insuranceCompanyRepository.findAll();
    }
}
