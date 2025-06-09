package com.medical.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "fin_bet")
@IdClass(FinBet.FinBetId.class)
public class FinBet {
    @Id
    @ManyToOne
    @JoinColumn(name = "bet_id", referencedColumnName = "bet_id")
    private Bet bet;

    @Id
    @ManyToOne
    @JoinColumn(name = "visit_id", referencedColumnName = "visit_id")
    private Visit visit;

    // Constructors, getters, setters, etc.

    public static class FinBetId implements Serializable {
        private Integer bet;
        private Integer visit;

        // Default constructor
        public FinBetId() {}

        public FinBetId(Integer bet, Integer visit) {
            this.bet = bet;
            this.visit = visit;
        }

        // Getters and setters
        // equals() and hashCode() methods
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FinBetId that = (FinBetId) o;
            return Objects.equals(visit, that.visit) &&
                    Objects.equals(bet, that.bet);
        }

        @Override
        public int hashCode() {
            return Objects.hash(bet, visit);
        }
    }

    public FinBet() {}

    public FinBet(Bet bet, Visit visit) {
        this.bet = bet;
        this.visit = visit;
    }

    public Visit getVisit() {
        return visit;
    }

    public void setVisit(Visit visit) {
        this.visit = visit;
    }

    public Bet getBet() {
        return bet;
    }

    public void setBet(Bet bet) {
        this.bet = bet;
    }
}