package com.medical.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InsuranceCheckRequest {
    private String firstName;
    private String lastName;
    private Integer insuranceCompanyId;

    public Integer getInsuranceCompanyId()
    {
        return insuranceCompanyId;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }
}