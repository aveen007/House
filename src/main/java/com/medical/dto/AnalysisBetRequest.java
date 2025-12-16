package com.medical.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AnalysisBetRequest {
    @NotNull(message = "patientId ID cannot be null")
    private Integer betId;

    @NotBlank(message = "patient analysis ID cannot be null")
    private Integer patientAnalysisId;

    public Integer getBetId(){
        return betId;
    }

    public Integer getPatientAnalysisId(){
        return patientAnalysisId;
    }
}
