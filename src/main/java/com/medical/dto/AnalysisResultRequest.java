package com.medical.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AnalysisResultRequest {
    @NotNull(message = "patientAnalysisId ID cannot be null")
    private Integer patientAnalysisId;

    @NotBlank(message = "Result cannot be null")
    private String result;

    public Integer getPatientAnalysisId(){
        return patientAnalysisId;
    }

    public String getResult(){
        return result;
    }
}
