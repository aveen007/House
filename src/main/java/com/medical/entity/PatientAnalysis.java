package com.medical.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "patient_analysis")
public class PatientAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotNull(message = "Patient Analysis must have a patient")
    @Column(name = "patient_id", nullable = false)
    private Integer patientId;

    @NotNull(message = "Patient Analysis must have a analysis")
    @Column(name = "analysis_id", nullable = false)
    private Integer analysisId;

    @NotNull(message = "Analysis date is required")
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @NotNull(message = "Patient Analysis must have a status")
    @Column(name = "status", nullable = false)
    @Convert(converter = AnalysisStatusConverter.class)
    private AnalysisStatus status;
}
