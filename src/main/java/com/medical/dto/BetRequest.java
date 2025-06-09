package com.medical.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;


@Data
public class BetRequest {
    @NotNull
    private Integer visitId;

    @NotBlank
    private String diagnosis;

    @NotNull
    private Long amount;

    public Integer getVisitId()
    {
        return visitId;
    }
    public String getDiagnosis()
    {
        return diagnosis;
    }
    public Long getAmount()
    {
        return amount;
    }

    public void setVisitId(Integer visitId) {
        this.visitId = visitId;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public void setGender(Long amount) {
        this.amount = amount;
    }
}