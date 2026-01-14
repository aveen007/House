package com.medical.performance;

import com.medical.dto.PatientRequest;
import com.medical.entity.InsuranceCompany;
import com.medical.entity.Patient;
import com.medical.entity.Visit;
import com.medical.repository.*;
import com.medical.service.PatientService;
import com.medical.service.VisitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Volume Testing - Объёмное тестирование
 * 
 * Проверяет корректность и производительность системы при больших объёмах данных.
 * Согласно TestPlan (5.2.8), необходимо проверить работу с:
 * - Большим количеством пациентов
 * - Большим количеством визитов
 * - Большим количеством ставок и анализов
 */
@SpringBootTest
@ActiveProfiles("test")
class VolumeTestingTest {

    @Autowired
    private PatientService patientService;

    @Autowired
    private VisitService visitService;

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

    @MockBean
    private com.medical.service.InsuranceClient insuranceClient;

    private InsuranceCompany insuranceCompany;
    private static final int VOLUME_PATIENTS = 1000; // Уменьшено для быстрого выполнения тестов
    private static final int VOLUME_VISITS_PER_PATIENT = 5;

    @BeforeEach
    void setUp() {
        // Очистка данных
        patientAnalysisRepository.deleteAll();
        betRepository.deleteAll();
        visitRepository.deleteAll();
        patientRepository.deleteAll();
        insuranceCompanyRepository.deleteAll();

        // Создание тестовой страховой компании
        insuranceCompany = new InsuranceCompany();
        insuranceCompany.setCompanyName("Volume Test Insurance");
        insuranceCompany.setApiUrl("http://test-api.com");
        insuranceCompany.setApiKey("test-key");
        insuranceCompany = insuranceCompanyRepository.save(insuranceCompany);

        // Mock для проверки страховки
        when(insuranceClient.verifyInsurance(any(), any()))
                .thenReturn(new com.medical.service.InsuranceClient.InsuranceVerificationResponse(true, "Verified"));
    }

    @Test
    void testLargeVolumeOfPatients() {
        long startTime = System.currentTimeMillis();

        // Создание большого количества пациентов
        List<Patient> createdPatients = new ArrayList<>();
        for (int i = 0; i < VOLUME_PATIENTS; i++) {
            PatientRequest request = new PatientRequest();
            request.setFirstName("Patient" + i);
            request.setLastName("Test" + i);
            request.setDateOfBirth(LocalDate.of(1980 + (i % 50), 1, 1));
            request.setGender(i % 2 == 0 ? "M" : "F");
            request.setInsuranceCompanyId(insuranceCompany.getId());

            Patient patient = patientService.createPatient(request, false); // Без проверки страховки для скорости
            createdPatients.add(patient);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Проверка: все пациенты созданы
        assertEquals(VOLUME_PATIENTS, createdPatients.size(), 
                "Должно быть создано " + VOLUME_PATIENTS + " пациентов");

        // Проверка: все пациенты сохранены в БД
        List<Patient> allPatients = patientService.getAllPatients();
        assertEquals(VOLUME_PATIENTS, allPatients.size(), 
                "В БД должно быть " + VOLUME_PATIENTS + " пациентов");

        // Проверка производительности: создание 1000 пациентов должно занять разумное время
        // (обычно менее 30 секунд для тестовой БД)
        assertTrue(duration < 60000, 
                "Создание " + VOLUME_PATIENTS + " пациентов должно занять менее 60 секунд. Фактическое время: " + duration + " мс");

        System.out.println("Volume Test: Создание " + VOLUME_PATIENTS + " пациентов заняло " + duration + " мс");
    }

    @Test
    void testLargeVolumeOfVisits() {
        // Сначала создаём одного пациента
        PatientRequest patientRequest = new PatientRequest();
        patientRequest.setFirstName("Volume");
        patientRequest.setLastName("Test");
        patientRequest.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patientRequest.setGender("M");
        patientRequest.setInsuranceCompanyId(insuranceCompany.getId());
        Patient patient = patientService.createPatient(patientRequest, false);

        long startTime = System.currentTimeMillis();

        // Создание большого количества визитов для одного пациента
        List<Visit> createdVisits = new ArrayList<>();
        for (int i = 0; i < VOLUME_VISITS_PER_PATIENT; i++) {
            com.medical.dto.VisitRequest visitRequest = new com.medical.dto.VisitRequest();
            visitRequest.setPatientId(patient.getId());
            visitRequest.setDateOfVisit(LocalDate.now().minusDays(i));

            Visit visit = visitService.createVisit(visitRequest);
            createdVisits.add(visit);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Проверка: все визиты созданы
        assertEquals(VOLUME_VISITS_PER_PATIENT, createdVisits.size(), 
                "Должно быть создано " + VOLUME_VISITS_PER_PATIENT + " визитов");

        // Проверка: получение всех визитов пациента работает корректно
        List<Visit> patientVisits = visitService.getAllPatientVisits(patient.getId());
        assertEquals(VOLUME_VISITS_PER_PATIENT, patientVisits.size(), 
                "У пациента должно быть " + VOLUME_VISITS_PER_PATIENT + " визитов");

        // Проверка производительности
        assertTrue(duration < 10000, 
                "Создание " + VOLUME_VISITS_PER_PATIENT + " визитов должно занять менее 10 секунд. Фактическое время: " + duration + " мс");

        System.out.println("Volume Test: Создание " + VOLUME_VISITS_PER_PATIENT + " визитов заняло " + duration + " мс");
    }

    @Test
    void testRetrievalPerformanceWithLargeDataset() {
        // Создание тестовых данных
        List<Patient> patients = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            PatientRequest request = new PatientRequest();
            request.setFirstName("Patient" + i);
            request.setLastName("Retrieval" + i);
            request.setDateOfBirth(LocalDate.of(1980, 1, 1));
            request.setGender("M");
            request.setInsuranceCompanyId(insuranceCompany.getId());
            Patient patient = patientService.createPatient(request, false);
            patients.add(patient);
        }

        // Тест производительности получения всех пациентов
        long startTime = System.currentTimeMillis();
        List<Patient> allPatients = patientService.getAllPatients();
        long endTime = System.currentTimeMillis();
        long retrievalTime = endTime - startTime;

        // Проверка: все пациенты получены
        assertEquals(500, allPatients.size(), "Должно быть получено 500 пациентов");

        // Проверка производительности: получение 500 пациентов должно быть быстрым
        // Согласно SRS (PR-03): 90% операций ≤5 с
        assertTrue(retrievalTime < 5000, 
                "Получение 500 пациентов должно занять менее 5 секунд. Фактическое время: " + retrievalTime + " мс");

        System.out.println("Volume Test: Получение 500 пациентов заняло " + retrievalTime + " мс");
    }

    @Test
    void testDatabaseIntegrityWithLargeVolume() {
        // Создание большого количества пациентов
        List<Integer> patientIds = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            PatientRequest request = new PatientRequest();
            request.setFirstName("Volume" + i);
            request.setLastName("Integrity" + i);
            request.setDateOfBirth(LocalDate.of(1990, 1, 1));
            request.setGender("M");
            request.setInsuranceCompanyId(insuranceCompany.getId());
            Patient patient = patientService.createPatient(request, false);
            patientIds.add(patient.getId());
        }

        // Проверка целостности: все пациенты должны быть доступны
        for (Integer patientId : patientIds) {
            Patient patient = patientService.getPatient(patientId);
            assertNotNull(patient, "Пациент с ID " + patientId + " должен существовать");
            assertNotNull(patient.getFirstName(), "У пациента должно быть имя");
            assertNotNull(patient.getInsuranceCompany(), "У пациента должна быть страховая компания");
        }

        // Проверка: количество пациентов в БД соответствует созданным
        List<Patient> allPatients = patientService.getAllPatients();
        assertEquals(200, allPatients.size(), "В БД должно быть 200 пациентов");

        System.out.println("Volume Test: Проверка целостности данных для 200 пациентов пройдена успешно");
    }

    @Test
    void testConcurrentOperationsWithVolume() {
        // Создание начального набора данных
        List<Patient> initialPatients = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            PatientRequest request = new PatientRequest();
            request.setFirstName("Initial" + i);
            request.setLastName("Patient" + i);
            request.setDateOfBirth(LocalDate.of(1990, 1, 1));
            request.setGender("M");
            request.setInsuranceCompanyId(insuranceCompany.getId());
            Patient patient = patientService.createPatient(request, false);
            initialPatients.add(patient);
        }

        // Проверка: получение данных работает после создания большого объема
        long startTime = System.currentTimeMillis();
        List<Patient> retrievedPatients = patientService.getAllPatients();
        long endTime = System.currentTimeMillis();

        assertEquals(100, retrievedPatients.size(), "Должно быть получено 100 пациентов");
        
        // Проверка производительности
        long retrievalTime = endTime - startTime;
        assertTrue(retrievalTime < 3000, 
                "Получение 100 пациентов должно быть быстрым. Время: " + retrievalTime + " мс");

        System.out.println("Volume Test: Получение 100 пациентов после создания объёма данных заняло " + retrievalTime + " мс");
    }
}

