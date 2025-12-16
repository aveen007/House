package com.medical.service;

import com.medical.dto.BetPatientsResponse;
import com.medical.dto.BetRequest;
import com.medical.dto.VisitPatientsResponse;
import com.medical.entity.*;
import com.medical.exception.ResourceNotFoundException;
import com.medical.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BetService {
    private final PatientRepository patientRepository;
    private final VisitRepository visitRepository;
    private final BetRepository betRepository;
    private final FinBetRepository finBetRepository;
    private final VisitSymptomRepository visitSymptomRepository;

    public List<BetPatientsResponse> getAllBetPatients() {

        List<BetPatientsResponse> betPatients = new ArrayList<>();

        List<Patient> allPatients = patientRepository.findAll();

        for(Patient patient : allPatients)
        {
            List<Visit> RecentVisits = visitRepository.findByPatientIdOrderByDateOfVisitDesc(patient.getId());
            if(!RecentVisits.isEmpty())
            {
                Visit recentVisit = RecentVisits.get(0);
                List<Bet> allBets = betRepository.findByVisitId(recentVisit.getId());

                boolean betEnded = false;
                for(Bet bet : allBets)
                {
                    if(finBetRepository.findByBet(bet).isPresent()) {
                        betEnded = true;
                        break;
                    }
                }
                if(!betEnded) {
                    BetPatientsResponse betPatient = new BetPatientsResponse();

                    betPatient.setId(patient.getId());
                    betPatient.setVisitId(recentVisit.getId());
                    betPatient.setFirstName(patient.getFirstName());
                    betPatient.setLastName(patient.getLastName());
                    betPatient.setGender(patient.getGender());
                    betPatient.setInsuranceCompany(patient.getInsuranceCompany());
                    betPatient.setDateOfBirth(patient.getDateOfBirth());

                    betPatients.add(betPatient);
                }
            }
        }
        return  betPatients;
    }
    public List<VisitPatientsResponse> getAllVisitPatients() {

        List<VisitPatientsResponse> visitPatients = new ArrayList<>();

        List<Patient> allPatients = patientRepository.findAll();

        for(Patient patient : allPatients)
        {
            List<Visit> RecentVisits = visitRepository.findByPatientIdOrderByDateOfVisitDesc(patient.getId());
            if(!RecentVisits.isEmpty())
            {
                Visit recentVisit = RecentVisits.get(0);
                List<Bet> allBets = betRepository.findByVisitId(recentVisit.getId());


                VisitPatientsResponse visitPatient = new VisitPatientsResponse();

                    visitPatient.setId(patient.getId());
                    visitPatient.setVisitId(recentVisit.getId());
                    visitPatient.setFirstName(patient.getFirstName());
                    visitPatient.setLastName(patient.getLastName());
                    visitPatient.setGender(patient.getGender());
                    visitPatient.setInsuranceCompany(patient.getInsuranceCompany());
                    visitPatient.setDateOfBirth(patient.getDateOfBirth());
                    visitPatients.add(visitPatient);

            }
        }
        return  visitPatients;
    }

    @Transactional
    public Bet createBet(BetRequest request)
    {
        if(visitRepository.existsById(request.getVisitId())) {
            Bet bet = new Bet();
            bet.setVisitId(request.getVisitId());
            bet.setDiagnosis(request.getDiagnosis());
            bet.setAmount(request.getAmount());

            betRepository.save(bet);

            return bet;
        }
        else
            throw new ResourceNotFoundException("Visit not found");
    }

    public List<Integer> getVisitSymptoms(Integer visitId)
    {
        List<VisitSymptom> rowList = visitSymptomRepository.findByVisitId(visitId);
        System.out.println(rowList);
        List<Integer> symptomIds = new ArrayList<>(rowList.size());

        for (VisitSymptom visitSymptom : rowList) {
            symptomIds.add(visitSymptom.getSymptom().getId());
        }

        return symptomIds;
    }

    @Transactional
    public FinBet finalizeBet(Bet request)
    {
        Visit visit = visitRepository.findById(request.getVisitId())
                        .orElseThrow(() -> new ResourceNotFoundException("Visit not found"));
        Bet bet = betRepository.findById(request.getBetId())
                .orElseThrow(() -> new ResourceNotFoundException("Bet not found"));

        FinBet finBet = new FinBet();

        finBet.setBet(bet);
        finBet.setVisit(visit);

        finBetRepository.save(finBet);

        return finBet;
    }

    public List<Bet> getBetsForVisit(Integer visitId) {
        return betRepository.findByVisitId(visitId);
    }

    public Optional<FinBet> getStatusForVisit(Integer visitId)
    {
        List<Bet> allBets = betRepository.findByVisitId(visitId);

        for(Bet bet : allBets)
        {
            Optional<FinBet> optionalFinBet = finBetRepository.findByBet(bet);
            if(optionalFinBet.isPresent()) {
                return optionalFinBet;
            }
        }


        return Optional.empty();
    }
}
