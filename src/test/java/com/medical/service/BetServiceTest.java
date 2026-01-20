package com.medical.service;

import com.medical.dto.BetRequest;
import com.medical.entity.Bet;
import com.medical.entity.FinBet;
import com.medical.entity.Visit;
import com.medical.exception.ResourceNotFoundException;
import com.medical.repository.*;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BetServiceTest {

    PatientRepository patientRepository = mock(PatientRepository.class);
    VisitRepository visitRepository = mock(VisitRepository.class);
    BetRepository betRepository = mock(BetRepository.class);
    FinBetRepository finBetRepository = mock(FinBetRepository.class);
    VisitSymptomRepository visitSymptomRepository = mock(VisitSymptomRepository.class);

    BetService betService = new BetService(
            patientRepository,
            visitRepository,
            betRepository,
            finBetRepository,
            visitSymptomRepository
    );

    @Test
    void createBet_success() {
        // TC-FR09-01: Успешное создание ставки (Основной поток)
        BetRequest request = new BetRequest();
        request.setVisitId(1);
        request.setDiagnosis("Pneumonia");
        request.setAmount(500L);

        Visit visit = new Visit();
        visit.setId(1);
        visit.setPatientId(1);

        when(visitRepository.existsById(1)).thenReturn(true);
        when(betRepository.save(any(Bet.class))).thenAnswer(inv -> {
            Bet bet = inv.getArgument(0);
            bet.setBetId(10);
            return bet;
        });

        Bet saved = betService.createBet(request);

        assertNotNull(saved.getBetId());
        assertEquals(1, saved.getVisitId());
        assertEquals("Pneumonia", saved.getDiagnosis());
        assertEquals(500L, saved.getAmount());
        verify(betRepository).save(any(Bet.class));
    }

    @Test
    void createBet_visitNotFound_throws() {
        // TC-FR09-02: Создание ставки для несуществующего визита (Альтернативный поток)
        BetRequest request = new BetRequest();
        request.setVisitId(999);
        request.setDiagnosis("Pneumonia");
        request.setAmount(500L);

        when(visitRepository.existsById(999)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> betService.createBet(request));
        verify(betRepository, never()).save(any());
    }

    @Test
    void finalizeBet_success() {
        // TC-FR11-01: Успешная финализация ставки (Основной поток)
        Bet betRequest = new Bet();
        betRequest.setBetId(1);
        betRequest.setVisitId(1);
        betRequest.setDiagnosis("Pneumonia");
        betRequest.setAmount(500L);

        Visit visit = new Visit();
        visit.setId(1);
        visit.setPatientId(1);

        Bet bet = new Bet();
        bet.setBetId(1);
        bet.setVisitId(1);
        bet.setDiagnosis("Pneumonia");
        bet.setAmount(500L);

        when(visitRepository.findById(1)).thenReturn(Optional.of(visit));
        when(betRepository.findById(1)).thenReturn(Optional.of(bet));
        when(finBetRepository.save(any(FinBet.class))).thenAnswer(inv -> inv.getArgument(0));

        FinBet finBet = betService.finalizeBet(betRequest);

        assertNotNull(finBet);
        assertEquals(bet, finBet.getBet());
        assertEquals(visit, finBet.getVisit());
        verify(finBetRepository).save(any(FinBet.class));
    }

    @Test
    void finalizeBet_visitNotFound_throws() {
        // TC-FR11-02: Финализация ставки с несуществующим визитом (Альтернативный поток)
        Bet betRequest = new Bet();
        betRequest.setBetId(1);
        betRequest.setVisitId(999);

        when(visitRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> betService.finalizeBet(betRequest));
        verify(finBetRepository, never()).save(any());
    }

    @Test
    void finalizeBet_betNotFound_throws() {
        // TC-FR11-02: Финализация несуществующей ставки (Альтернативный поток)
        Bet betRequest = new Bet();
        betRequest.setBetId(999);
        betRequest.setVisitId(1);

        Visit visit = new Visit();
        visit.setId(1);
        visit.setPatientId(1);

        when(visitRepository.findById(1)).thenReturn(Optional.of(visit));
        when(betRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> betService.finalizeBet(betRequest));
        verify(finBetRepository, never()).save(any());
    }
}

