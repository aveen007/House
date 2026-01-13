package com.medical.dto;

public class ContractSignRequest {
    private Integer patientId;
    private String signedBy;
    private String signature;

    public Integer getPatientId() { return patientId; }
    public void setPatientId(Integer patientId) { this.patientId = patientId; }

    public String getSignedBy() { return signedBy; }
    public void setSignedBy(String signedBy) { this.signedBy = signedBy; }

    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
}
