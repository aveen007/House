package com.medical.dto;

import java.time.OffsetDateTime;

public class ContractViewResponse {
    private Integer contractId;
    private Integer patientId;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime signedAt;

    private Integer termsId;
    private Integer termsVersion;
    private String termsTitle;
    private String termsContent; // можно отдавать snapshot или content — ниже объясню
    private String termsSnapshot;

    public Integer getContractId() { return contractId; }
    public void setContractId(Integer contractId) { this.contractId = contractId; }

    public Integer getPatientId() { return patientId; }
    public void setPatientId(Integer patientId) { this.patientId = patientId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public OffsetDateTime getSignedAt() { return signedAt; }
    public void setSignedAt(OffsetDateTime signedAt) { this.signedAt = signedAt; }

    public Integer getTermsId() { return termsId; }
    public void setTermsId(Integer termsId) { this.termsId = termsId; }

    public Integer getTermsVersion() { return termsVersion; }
    public void setTermsVersion(Integer termsVersion) { this.termsVersion = termsVersion; }

    public String getTermsTitle() { return termsTitle; }
    public void setTermsTitle(String termsTitle) { this.termsTitle = termsTitle; }

    public String getTermsContent() { return termsContent; }
    public void setTermsContent(String termsContent) { this.termsContent = termsContent; }

    public String getTermsSnapshot() { return termsSnapshot; }
    public void setTermsSnapshot(String termsSnapshot) { this.termsSnapshot = termsSnapshot; }
}
