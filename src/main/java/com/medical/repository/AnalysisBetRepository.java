package com.medical.repository;

import com.medical.entity.AnalysisBet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnalysisBetRepository extends JpaRepository<AnalysisBet, Integer> {
    List<AnalysisBet> findByBet_BetId(Integer betId);
}
