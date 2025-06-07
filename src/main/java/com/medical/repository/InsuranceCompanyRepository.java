package com.medical.repository;

import com.medical.entity.InsuranceCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InsuranceCompanyRepository extends JpaRepository<InsuranceCompany, Integer> {
    Optional<InsuranceCompany> findByCompanyName(String companyName);
}