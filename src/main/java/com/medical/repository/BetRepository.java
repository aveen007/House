package com.medical.repository;

import com.medical.entity.Bet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BetRepository extends JpaRepository<Bet, Integer> {
    List<Bet> findByVisitId(Integer visitId);
    boolean existsByVisitId(Integer visitId);
}