package com.medical.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class VisitRequest {

    @NotNull
    private Integer patientId;

    @NotNull
    private LocalDate dateOfVisit;

    public Integer getPatientId() {
        return patientId;
    }

    public LocalDate getDateOfVisit() {
        return dateOfVisit;
    }
}
