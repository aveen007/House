package com.medical.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "analysis_bet")
@IdClass(AnalysisBet.BetAnalysisId.class)
public class AnalysisBet {
    @Id
    @ManyToOne
    @JoinColumn(name = "bet_id", referencedColumnName = "bet_id")
    private Bet bet;

    @Id
    @ManyToOne
    @JoinColumn(name = "patient_analysis_id", referencedColumnName = "id")
    private PatientAnalysis patientAnalysis;

    // Constructors, getters, setters, etc.

    public static class BetAnalysisId implements Serializable {
        private Integer bet;
        private Integer patientAnalysis;

        // Default constructor
        public BetAnalysisId() {}

        public BetAnalysisId(Integer bet, Integer patientAnalysis) {
            this.bet = bet;
            this.patientAnalysis = patientAnalysis;
        }

        // Getters and setters
        // equals() and hashCode() methods
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BetAnalysisId that = (BetAnalysisId) o;
            return Objects.equals(bet, that.bet) &&
                    Objects.equals(patientAnalysis, that.patientAnalysis);
        }

        @Override
        public int hashCode() {
            return Objects.hash(bet, patientAnalysis);
        }
    }

    // Default constructor
    public AnalysisBet() {}

    public AnalysisBet(Bet bet, PatientAnalysis patientAnalysis) {
        this.bet = bet;
        this.patientAnalysis = patientAnalysis;
    }

    // Getters and setters
    public Bet getBet() {
        return bet;
    }

    public void setBet(Bet bet) {
        this.bet = bet;
    }

    public PatientAnalysis getPatientAnalysis() {
        return patientAnalysis;
    }

    public void setPatientAnalysis(PatientAnalysis patientAnalysis) {
        this.patientAnalysis = patientAnalysis;
    }
}