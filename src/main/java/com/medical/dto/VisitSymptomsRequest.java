package com.medical.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class VisitSymptomsRequest {

    private List<Integer> symptomIds;

    public List<Integer> getSymptomIds() {
        return symptomIds;
    }
}
