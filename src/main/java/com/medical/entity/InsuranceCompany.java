package com.medical.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "insurance_companies")
public class InsuranceCompany {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "insurance_company_id")
    private Integer id;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "api_url", nullable = false)
    private String apiUrl;

    @Column(name = "key", nullable = false)
    private String apiKey;

    public String getApiUrl()
    {
        return apiUrl;
    }

    public String getApiKey()
    {
        return apiKey;
    }

    public Integer getId()
    {
        return id;
    }

    public String getCompanyName()
    {
        return companyName;
    }
}