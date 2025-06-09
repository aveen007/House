package com.medical.repository;

import com.medical.entity.Bet;
import com.medical.entity.FinBet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface FinBetRepository extends JpaRepository<FinBet, Integer> {
    Optional<FinBet> findByBet(Bet bet);
}