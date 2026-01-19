package com.medical.integration;

import com.medical.dto.BetRequest;
import com.medical.dto.PatientAnalysisRequest;
import com.medical.dto.PatientRequest;
import com.medical.dto.VisitRequest;
import com.medical.entity.*;
import com.medical.repository.*;
import com.medical.security.SecurityUtils;
import com.medical.service.AnalysisService;
import com.medical.service.BetService;
import com.medical.service.PatientService;
import com.medical.service.VisitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Business Cycle Testing - Сквозное тестирование бизнес-цикла пациента
 * 
 * Проверяет полный цикл: Регистрация пациента → Создание визита → 
 * Создание ставки → Создание анализа → Обновление статусов
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BusinessCycleTest {

    @Autowired
    private PatientService patientService;

    @Autowired
    private VisitService visitService;

    @Autowired
    private BetService betService;

    @Autowired
    private AnalysisService analysisService;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private BetRepository betRepository;

    @Autowired
    private PatientAnalysisRepository patientAnalysisRepository;

    @Autowired
    private InsuranceCompanyRepository insuranceCompanyRepository;

    @Autowired
    private AnalysisRepository analysisRepository;

    @MockBean
    private com.medical.service.InsuranceClient insuranceClient;

    @MockBean
    private SecurityUtils securityUtils;

    private InsuranceCompany insuranceCompany;
    private Analysis analysis;

    @BeforeEach
    void setUp() {
        // Очистка данных перед каждым тестом
        patientAnalysisRepository.deleteAll();
        betRepository.deleteAll();
        visitRepository.deleteAll();
        patientRepository.deleteAll();
        analysisRepository.deleteAll();
        insuranceCompanyRepository.deleteAll();

        // Создание тестовой страховой компании
        insuranceCompany = new InsuranceCompany();
        insuranceCompany.setCompanyName("Test Insurance Company");
        insuranceCompany.setApiUrl("http://test-api.com");
        insuranceCompany.setApiKey("test-key");
        insuranceCompany = insuranceCompanyRepository.save(insuranceCompany);

        // Создание тестового анализа
        analysis = new Analysis();
        analysis.setTitle("Blood Test");
        analysis = analysisRepository.save(analysis);

        // Mock для проверки страховки
        when(insuranceClient.verifyInsurance(any(), any()))
                .thenReturn(new com.medical.service.InsuranceClient.InsuranceVerificationResponse(true, "Verified"));

        // Mock для security utils (без проверки доступа в тестах)
        org.mockito.Mockito.doNothing().when(securityUtils).assertPatientAccess(any());
    }

    @Test
    void testCompleteBusinessCycle() {
        // Шаг 1: Регистрация пациента (FR-01)
        PatientRequest patientRequest = new PatientRequest();
        patientRequest.setFirstName("John");
        patientRequest.setLastName("Doe");
        patientRequest.setDateOfBirth(LocalDate.of(1980, 5, 15));
        patientRequest.setGender("M");
        patientRequest.setInsuranceCompanyId(insuranceCompany.getId());

        Patient patient = patientService.createPatient(patientRequest, true);
        assertNotNull(patient.getId(), "Пациент должен быть создан с ID");
        assertEquals("John", patient.getFirstName(), "Имя пациента должно совпадать");
        assertEquals("Doe", patient.getLastName(), "Фамилия пациента должна совпадать");

        // Шаг 2: Проверка страховки (FR-03)
        // Проверка страховки выполняется автоматически при создании пациента
        assertNotNull(patient.getInsuranceCompany(), "У пациента должна быть страховая компания");
        assertEquals(insuranceCompany.getId(), patient.getInsuranceCompany().getId(), 
                "ID страховой компании должен совпадать");

        // Шаг 3: Создание визита (FR-02)
        VisitRequest visitRequest = new VisitRequest();
        visitRequest.setPatientId(patient.getId());
        visitRequest.setDateOfVisit(LocalDate.now());

        Visit visit = visitService.createVisit(visitRequest);
        assertNotNull(visit.getId(), "Визит должен быть создан с ID");
        assertEquals(patient.getId(), visit.getPatientId(), "ID пациента в визите должен совпадать");
        assertEquals(VisitHDStatus.Awaiting, visit.getHdStatus(), 
                "Статус визита должен быть Awaiting по умолчанию");

        // Шаг 4: Обновление статуса визита (изменение статуса на Accepted)
        Visit updatedVisit = visitService.updateVisitHDStatus(visit.getId(), VisitHDStatus.Accepted);
        assertEquals(VisitHDStatus.Accepted, updatedVisit.getHdStatus(), 
                "Статус визита должен быть обновлен на Accepted");

        // Шаг 5: Создание ставки (FR-09)
        BetRequest betRequest = new BetRequest();
        betRequest.setVisitId(visit.getId());
        betRequest.setDiagnosis("Pneumonia");
        betRequest.setAmount(500L);

        Bet bet = betService.createBet(betRequest);
        assertNotNull(bet.getBetId(), "Ставка должна быть создана с ID");
        assertEquals(visit.getId(), bet.getVisitId(), "ID визита в ставке должен совпадать");
        assertEquals("Pneumonia", bet.getDiagnosis(), "Диагноз должен совпадать");
        assertEquals(500L, bet.getAmount(), "Сумма ставки должна совпадать");

        // Шаг 6: Создание анализа пациента (FR-13-16)
        PatientAnalysisRequest analysisRequest = new PatientAnalysisRequest();
        analysisRequest.setPatientId(patient.getId());
        analysisRequest.setAnalysisId(analysis.getId());
        analysisRequest.setBetId(bet.getBetId());
        analysisRequest.setDate(LocalDate.now());
        analysisRequest.setStatus(AnalysisStatus.AwaitingHD);

        PatientAnalysis patientAnalysis = analysisService.createPatientAnalysis(analysisRequest);
        assertNotNull(patientAnalysis.getId(), "Анализ пациента должен быть создан с ID");
        assertEquals(patient.getId(), patientAnalysis.getPatientId(), 
                "ID пациента в анализе должен совпадать");
        assertEquals(AnalysisStatus.AwaitingHD, patientAnalysis.getStatus(), 
                "Статус анализа должен быть AwaitingHD");

        // Шаг 7: Обновление статуса анализа (FR-13-16)
        PatientAnalysis updatedAnalysis = analysisService.updatePatientAnalysisStatus(
                patientAnalysis.getId(), AnalysisStatus.Accepted);
        assertEquals(AnalysisStatus.Accepted, updatedAnalysis.getStatus(), 
                "Статус анализа должен быть обновлен на Accepted");

        // Шаг 8: Проверка целостности данных - получение всех визитов пациента
        List<Visit> patientVisits = visitService.getAllPatientVisits(patient.getId());
        assertFalse(patientVisits.isEmpty(), "У пациента должен быть хотя бы один визит");
        assertEquals(1, patientVisits.size(), "У пациента должен быть ровно один визит");

        // Шаг 9: Проверка целостности данных - получение всех анализов пациента
        List<PatientAnalysis> patientAnalyses = analysisService.getPatientAnalyses(patient.getId());
        assertFalse(patientAnalyses.isEmpty(), "У пациента должен быть хотя бы один анализ");
        assertEquals(1, patientAnalyses.size(), "У пациента должен быть ровно один анализ");

        // Финальная проверка: все данные связаны корректно
        Patient finalPatient = patientService.getPatient(patient.getId());
        assertNotNull(finalPatient, "Пациент должен существовать в БД");
        assertEquals("John", finalPatient.getFirstName(), "Имя пациента должно сохраниться");
    }

    @Test
    void testBusinessCycleWithMultipleVisits() {
        // Создание пациента
        PatientRequest patientRequest = new PatientRequest();
        patientRequest.setFirstName("Jane");
        patientRequest.setLastName("Smith");
        patientRequest.setDateOfBirth(LocalDate.of(1990, 3, 20));
        patientRequest.setGender("F");
        patientRequest.setInsuranceCompanyId(insuranceCompany.getId());

        Patient patient = patientService.createPatient(patientRequest, true);
        assertNotNull(patient.getId());

        // Создание первого визита
        VisitRequest visitRequest1 = new VisitRequest();
        visitRequest1.setPatientId(patient.getId());
        visitRequest1.setDateOfVisit(LocalDate.now().minusDays(7));
        Visit visit1 = visitService.createVisit(visitRequest1);
        assertNotNull(visit1.getId());

        // Создание второго визита
        VisitRequest visitRequest2 = new VisitRequest();
        visitRequest2.setPatientId(patient.getId());
        visitRequest2.setDateOfVisit(LocalDate.now());
        Visit visit2 = visitService.createVisit(visitRequest2);
        assertNotNull(visit2.getId());

        // Проверка: у пациента должно быть 2 визита
        List<Visit> allVisits = visitService.getAllPatientVisits(patient.getId());
        assertEquals(2, allVisits.size(), "У пациента должно быть 2 визита");
        
        // Проверка: визиты отсортированы по дате (новые первыми)
        assertEquals(visit2.getId(), allVisits.get(0).getId(), 
                "Первый визит в списке должен быть самым новым");
    }
}

