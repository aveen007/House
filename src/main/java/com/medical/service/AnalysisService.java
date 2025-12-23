package com.medical.service;

import com.medical.dto.AnalysisBetRequest;
import com.medical.dto.AnalysisResultRequest;
import com.medical.dto.PatientAnalysisRequest;
import com.medical.entity.*;
import com.medical.exception.ResourceNotFoundException;
import com.medical.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalysisService {
    private final AnalysisRepository analysisRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final PatientAnalysisRepository patientAnalysisRepository;
    private final PatientRepository patientRepository;
    private final AnalysisBetRepository analysisBetRepository;
    private final BetRepository betRepository;

    public List<Analysis> getAllAnalysis() {
        List<Analysis> allAnalyses  = analysisRepository.findAll();
        return  allAnalyses;
    }

    @Transactional
    public PatientAnalysis createPatientAnalysis(PatientAnalysisRequest request) {
        validatePatientAnalysisData(request);

        Bet existingBet = betRepository.findById(request.getBetId())
                .orElseThrow(() -> new ResourceNotFoundException("Bet not found"));

        PatientAnalysis analysis = new PatientAnalysis();
        analysis.setPatientId(request.getPatientId());
        analysis.setAnalysisId(request.getAnalysisId());
        analysis.setDate(request.getDate());
        analysis.setStatus(request.getStatus());

        var newPatientAnalysis = patientAnalysisRepository.save(analysis);

        AnalysisBet analysisBet = new AnalysisBet();
        analysisBet.setBet(existingBet);
        analysisBet.setPatientAnalysis(newPatientAnalysis);
        analysisBetRepository.save(analysisBet);

        return newPatientAnalysis;
    }

    @Transactional
    public PatientAnalysis updatePatientAnalysisStatus(Integer id, AnalysisStatus status) {
        // Find the existing patient analysis
        PatientAnalysis existingPatientAnalysis = patientAnalysisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PatientAnalysis not found"));

        // Update the  details
        existingPatientAnalysis.setStatus(status);

        // Save the updated
        return patientAnalysisRepository.save(existingPatientAnalysis);
    }

    @Transactional
    public AnalysisResult createAnalysisResult(AnalysisResultRequest request) {
        // Find the existing patient analysis
        PatientAnalysis existingPatientAnalysis = patientAnalysisRepository.findById(request.getPatientAnalysisId())
                .orElseThrow(() -> new ResourceNotFoundException("PatientAnalysis not found"));

        var analysisResult = new AnalysisResult();
        analysisResult.setPatientAnalyisId(request.getPatientAnalysisId());
        analysisResult.setResult(request.getResult());
        // Save the updated
        return analysisResultRepository.save(analysisResult);
    }

    public List<PatientAnalysis> getPatientAnalyses(Integer patient_id) {
        Patient existingPatient = patientRepository.findById(patient_id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        // Find the existing patient analysis
        var existingPatientAnalysis = patientAnalysisRepository.findByPatientId(patient_id);

        return existingPatientAnalysis;
    }

    public List<PatientAnalysis> getAwaitingHDAnalyses() {
        return patientAnalysisRepository.findByStatus(AnalysisStatus.AwaitingHD);
    }
    public List<PatientAnalysis> getApprovedAnalyses() {
        return patientAnalysisRepository.findByStatus(AnalysisStatus.Accepted);
    }

    public List<PatientAnalysis> getBetAnalyses(Integer bet_id) {
        Bet existingBet = betRepository.findById(bet_id)
                .orElseThrow(() -> new ResourceNotFoundException("Bet not found"));

        var analysisBetList = analysisBetRepository.findByBet_BetId(bet_id);

        List<PatientAnalysis> patientAnalyses = new ArrayList<>();
        for(AnalysisBet analysisBet : analysisBetList){
            patientAnalyses.add(analysisBet.getPatientAnalysis());
        }

        return patientAnalyses;
    }

    private void validatePatientAnalysisData(PatientAnalysisRequest request)
    {
        if(!patientRepository.existsById(request.getPatientId()))
        {
            throw new IllegalArgumentException("Patient with that id doesn't exist");
        }

        if(!analysisRepository.existsById(request.getAnalysisId()))
        {
            throw new IllegalArgumentException("Analysis with that id doesn't exist");
        }
    }
    public List<PatientAnalysis> getAllPatientAnalyses() {
        return patientAnalysisRepository.findAll();
    }
}

