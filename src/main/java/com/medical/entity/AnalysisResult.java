package com.medical.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
@Table(name = "analysis_result")
public class AnalysisResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotNull(message = "Result must have a patient_analysis_id")
    @Column(name = "patient_analysis_id", nullable = false)
    private Integer patientAnalysisId;

    @NotNull(message = "Result must have a result text")
    @Column(name = "result", nullable = false)
    private String result;

    public void setPatientAnalyisId(Integer patientAnalysisId) {
        this.patientAnalysisId = patientAnalysisId;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
