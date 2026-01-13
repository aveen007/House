package com.medical.dto;

public class ContractCreateRequest {
    private Integer patientId;
    private Integer termsId;

    public Integer getPatientId() { return patientId; }
    public void setPatientId(Integer patientId) { this.patientId = patientId; }

    public Integer getTermsId() { return termsId; }
    public void setTermsId(Integer termsId) { this.termsId = termsId; }
}
