package com.medical.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "bets")
public class Bet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bet_id", nullable = false)
    private Integer betId;

    @Column(name = "visit_id", nullable = false)
    private Integer visitId;

    @Column(name = "diagnosis", nullable = false)
    private String diagnosis;

    @Column(name = "amount", nullable = false)
    private Long amount;

    public Integer getBetId() {
        return betId;
    }

    public void setBetId(Integer betId) {
        this.betId = betId;
    }

    public Integer getVisitId() {
        return visitId;
    }

    public void setVisitId(Integer visitId) {
        this.visitId = visitId;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }
}
