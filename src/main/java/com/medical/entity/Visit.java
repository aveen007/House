package com.medical.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "visits")
public class Visit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "visit_id")
    private Integer id;

    @NotNull(message = "patient's id is required")
    @Column(name = "patient_id", nullable = false)
    private Integer patientId;

    @NotNull(message = "Visit date is required")
    @Column(name = "date_of_visit", nullable = false)
    private LocalDate dateOfVisit;

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public void setDateOfVisit(LocalDate dateOfVisit) {
        this.dateOfVisit = dateOfVisit;
    }
}
