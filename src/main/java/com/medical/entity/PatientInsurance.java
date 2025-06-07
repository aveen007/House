package com.medical.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "patient_insurance")
@Data
public class PatientInsurance {
    @Id
    private Integer id;
    private String patientFirstName;
    private String patientLastName;
    private String companyName;
}