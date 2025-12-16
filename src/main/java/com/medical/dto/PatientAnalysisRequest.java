package com.medical.dto;

import com.medical.entity.AnalysisStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class PatientAnalysisRequest {

    @NotNull(message = "Bet ID cannot be null")
    private Integer betId;

    @NotNull(message = "Patient ID cannot be null")
    private Integer patientId;

    @NotNull(message = "Analysis ID cannot be null")
    private Integer analysisId;

    @NotNull(message = "Date cannot be null")
    private LocalDate date;

    @NotNull(message = "Status cannot be null")
    private AnalysisStatus status;

    public Integer getBetId(){
        return betId;
    }
    public Integer getPatientId() {
        return patientId;
    }

    public Integer getAnalysisId() {
        return analysisId;
    }

    public LocalDate getDate() {
        return date;
    }

    public AnalysisStatus getStatus() {
        return status;
    }

    public void setBetId(Integer betId) {
        this.betId = betId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public void setAnalysisId(Integer analysisId) {
        this.analysisId = analysisId;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setStatus(AnalysisStatus status) {
        this.status = status;
    }
}