package com.medical.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

import com.medical.entity.Visit;
import com.medical.entity.Symptom;

@Entity
@Table(name = "visit_symptom")
@IdClass(VisitSymptom.VisitSymptomId.class)
public class VisitSymptom {

    @Id
    @ManyToOne
    @JoinColumn(name = "visit_id", referencedColumnName = "visit_id")
    private Visit visit;

    @Id
    @ManyToOne
    @JoinColumn(name = "symptom_id", referencedColumnName = "symptom_id")
    private Symptom symptom;

    // Constructors, getters, setters, etc.

    public static class VisitSymptomId implements Serializable {
        private Integer visit;
        private Integer symptom;

        // Default constructor
        public VisitSymptomId() {}

        public VisitSymptomId(Integer visit, Integer symptom) {
            this.visit = visit;
            this.symptom = symptom;
        }

        // Getters and setters
        // equals() and hashCode() methods
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VisitSymptomId that = (VisitSymptomId) o;
            return Objects.equals(visit, that.visit) &&
                    Objects.equals(symptom, that.symptom);
        }

        @Override
        public int hashCode() {
            return Objects.hash(visit, symptom);
        }
    }

    // Default constructor
    public VisitSymptom() {}

    public VisitSymptom(Visit visit, Symptom symptom) {
        this.visit = visit;
        this.symptom = symptom;
    }

    // Getters and setters
    public Visit getVisit() {
        return visit;
    }

    public void setVisit(Visit visit) {
        this.visit = visit;
    }

    public Symptom getSymptom() {
        return symptom;
    }

    public void setSymptom(Symptom symptom) {
        this.symptom = symptom;
    }
}