package com.medical.dto;

import com.medical.entity.Patient;

public class BetPatientsResponse extends Patient {
    public Integer visitId;
    public void setVisitId(Integer visitId){ this.visitId = visitId; }
}
