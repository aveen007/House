package com.medical.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Entity
@Table(name = "symptom")
public class Symptom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "symptom_id")
    private Integer id;

    @NotBlank(message = "Symptom name is required")
    @Column(name = "symptom_name", nullable = false)
    private String symptomName;

}
