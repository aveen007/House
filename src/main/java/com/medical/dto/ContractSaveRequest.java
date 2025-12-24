package com.medical.dto;

public class ContractSaveRequest {
    private Integer termsId; // можно менять только в DRAFT
    private String status;   // DRAFT или READY

    public Integer getTermsId() { return termsId; }
    public void setTermsId(Integer termsId) { this.termsId = termsId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
