package com.medical.performance;

import com.medical.dto.BetPatientsResponse;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Volume Testing - Объёмное тестирование
 * 
 * Согласно TestPlan 5.2.8:
 * 1. Наполнить БД тестовыми записями:
 *    - 10,000 пациентов (~50 MB)
 *    - 50,000 ставок (~150 MB)
 *    - 100,000 лабораторных анализов (~150 MB)
 * 2. Запустить ключевые операции: поиск пациента, формирование отчёта по ставкам, 
 *    открытие карты пациента с 1,000 записей истории
 * 3. Измерить время выполнения, потребление памяти, нагрузки на диск и индексы
 * 4. Проверить поведение бэкап/restore и время резервного копирования
 */
@SpringBootTest
@ActiveProfiles("test")
class VolumeTestingTest {

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
    private AnalysisBetRepository analysisBetRepository;

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
    
    // Объёмы данных согласно TestPlan 5.2.8
    private static final int VOLUME_PATIENTS = 10000;
    private static final int VOLUME_BETS = 50000;
    private static final int VOLUME_ANALYSES = 100000;
    private static final int HISTORY_VISITS_PER_PATIENT = 1000; // Для карты пациента с историей

    @BeforeEach
    void setUp() {
        // Очистка данных (важно: сначала удаляем зависимые таблицы)
        analysisBetRepository.deleteAll(); // Сначала удаляем связи
        patientAnalysisRepository.deleteAll();
        betRepository.deleteAll();
        visitRepository.deleteAll();
        patientRepository.deleteAll();
        analysisRepository.deleteAll();
        insuranceCompanyRepository.deleteAll();

        // Создание тестовой страховой компании
        insuranceCompany = new InsuranceCompany();
        insuranceCompany.setCompanyName("Volume Test Insurance");
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

    /**
     * Тест 1: Наполнение БД большими объёмами данных
     * Создание 10,000 пациентов, 50,000 ставок, 100,000 анализов
     */
    @Test
    void testFillDatabaseWithLargeVolumes() {
        System.out.println("=== Тест 1: Наполнение БД большими объёмами данных ===");
        
        // Измерение памяти до начала
        long memoryBefore = getUsedMemory();
        System.out.println("Память до наполнения: " + formatBytes(memoryBefore));

        long startTime = System.currentTimeMillis();

        // 1. Создание 10,000 пациентов
        System.out.println("Создание " + VOLUME_PATIENTS + " пациентов...");
        List<Patient> patients = new ArrayList<>();
        for (int i = 0; i < VOLUME_PATIENTS; i++) {
            PatientRequest request = new PatientRequest();
            request.setFirstName("Patient" + i);
            request.setLastName("Test" + i);
            request.setDateOfBirth(LocalDate.now().minusYears(20 + (i % 50)));
            request.setGender(i % 2 == 0 ? "M" : "F");
            request.setInsuranceCompanyId(insuranceCompany.getId());

            Patient patient = patientService.createPatient(request, false);
            patients.add(patient);
            
            if ((i + 1) % 1000 == 0) {
                System.out.println("  Создано пациентов: " + (i + 1));
            }
        }
        assertEquals(VOLUME_PATIENTS, patients.size(), "Должно быть создано " + VOLUME_PATIENTS + " пациентов");

        // 2. Создание визитов и ставок (50,000 ставок)
        System.out.println("Создание визитов и " + VOLUME_BETS + " ставок...");
        Random random = new Random();
        List<Visit> visits = new ArrayList<>();
        List<Bet> bets = new ArrayList<>();
        // Счетчик визитов для каждого пациента для обеспечения уникальности дат
        java.util.Map<Integer, Integer> patientVisitCounters = new java.util.HashMap<>();
        
        for (int i = 0; i < VOLUME_BETS; i++) {
            // Выбираем случайного пациента
            Patient patient = patients.get(random.nextInt(patients.size()));
            
            // Получаем счетчик визитов для этого пациента
            int visitCounter = patientVisitCounters.getOrDefault(patient.getId(), 0);
            patientVisitCounters.put(patient.getId(), visitCounter + 1);
            
            // Создаём визит для ставки с уникальной датой
            // Используем индекс цикла как основной фактор уникальности (он уникален для каждой итерации)
            VisitRequest visitRequest = new VisitRequest();
            visitRequest.setPatientId(patient.getId());
            // Базовая дата минус дни: используем индекс цикла для гарантии уникальности
            // Каждая итерация получает уникальную дату (базовый сдвиг 365 дней + уникальный индекс)
            visitRequest.setDateOfVisit(LocalDate.now().minusDays(365L + i));
            Visit visit = visitService.createVisit(visitRequest);
            visits.add(visit);
            
            // Обновляем статус визита на Accepted (для создания ставки)
            visitService.updateVisitHDStatus(visit.getId(), VisitHDStatus.Accepted);
            
            // Создаём ставку
            BetRequest betRequest = new BetRequest();
            betRequest.setVisitId(visit.getId());
            betRequest.setDiagnosis("Diagnosis " + i);
            betRequest.setAmount((long) (100 + random.nextInt(900)));
            Bet bet = betService.createBet(betRequest);
            bets.add(bet);
            
            if ((i + 1) % 5000 == 0) {
                System.out.println("  Создано ставок: " + (i + 1));
            }
        }
        assertEquals(VOLUME_BETS, bets.size(), "Должно быть создано " + VOLUME_BETS + " ставок");

        // 3. Создание 100,000 анализов
        System.out.println("Создание " + VOLUME_ANALYSES + " анализов...");
        List<PatientAnalysis> analyses = new ArrayList<>();
        for (int i = 0; i < VOLUME_ANALYSES; i++) {
            // Выбираем случайную ставку
            Bet bet = bets.get(random.nextInt(bets.size()));
            Patient patient = patients.get(random.nextInt(patients.size()));
            
            PatientAnalysisRequest analysisRequest = new PatientAnalysisRequest();
            analysisRequest.setPatientId(patient.getId());
            analysisRequest.setAnalysisId(analysis.getId());
            analysisRequest.setBetId(bet.getBetId());
            analysisRequest.setDate(LocalDate.now().minusDays(random.nextInt(365)));
            analysisRequest.setStatus(AnalysisStatus.AwaitingHD);
            
            PatientAnalysis patientAnalysis = analysisService.createPatientAnalysis(analysisRequest);
            analyses.add(patientAnalysis);
            
            if ((i + 1) % 10000 == 0) {
                System.out.println("  Создано анализов: " + (i + 1));
            }
        }
        assertEquals(VOLUME_ANALYSES, analyses.size(), "Должно быть создано " + VOLUME_ANALYSES + " анализов");

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Измерение памяти после наполнения
        long memoryAfter = getUsedMemory();
        long memoryUsed = memoryAfter - memoryBefore;
        
        System.out.println("\n=== Результаты наполнения БД ===");
        System.out.println("Время выполнения: " + duration + " мс (" + (duration / 1000.0) + " сек)");
        System.out.println("Память после наполнения: " + formatBytes(memoryAfter));
        System.out.println("Использовано памяти: " + formatBytes(memoryUsed));
        System.out.println("Пациентов: " + patients.size());
        System.out.println("Ставок: " + bets.size());
        System.out.println("Анализов: " + analyses.size());
        
        // Проверка целостности данных
        assertEquals(VOLUME_PATIENTS, patientRepository.count(), "Количество пациентов в БД должно совпадать");
        assertTrue(betRepository.count() >= VOLUME_BETS, "Количество ставок в БД должно быть не меньше " + VOLUME_BETS);
        assertTrue(patientAnalysisRepository.count() >= VOLUME_ANALYSES, 
                "Количество анализов в БД должно быть не меньше " + VOLUME_ANALYSES);
    }

    /**
     * Тест 2: Поиск пациента
     * Проверка производительности поиска пациента по имени при больших объёмах данных
     */
    @Test
    void testPatientSearchPerformance() {
        System.out.println("=== Тест 2: Поиск пациента ===");
        
        // Создание тестовых данных (1000 пациентов для быстрого теста)
        System.out.println("Создание тестовых данных...");
        List<Patient> patients = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            PatientRequest request = new PatientRequest();
            request.setFirstName("Search" + i);
            request.setLastName("Test" + i);
            request.setDateOfBirth(LocalDate.now().minusYears(20 + (i % 50)));
            request.setGender(i % 2 == 0 ? "M" : "F");
            request.setInsuranceCompanyId(insuranceCompany.getId());
            Patient patient = patientService.createPatient(request, false);
            patients.add(patient);
        }
        
        // Тест поиска: получение всех пациентов и фильтрация по имени
        System.out.println("Выполнение поиска пациента...");
        long startTime = System.currentTimeMillis();
        List<Patient> allPatients = patientService.getAllPatients();
        // Фильтрация по имени (имитация поиска)
        List<Patient> foundPatients = allPatients.stream()
                .filter(p -> p.getFirstName().contains("Search500"))
                .toList();
        long endTime = System.currentTimeMillis();
        long searchTime = endTime - startTime;
        
        System.out.println("Время поиска: " + searchTime + " мс");
        System.out.println("Найдено пациентов: " + foundPatients.size());
        
        // Проверка производительности: поиск должен быть ≤3 с (требование TestPlan)
        assertTrue(searchTime < 3000, 
                "Поиск пациента должен занимать ≤3 с. Фактическое время: " + searchTime + " мс");
        assertFalse(foundPatients.isEmpty(), "Должен быть найден хотя бы один пациент");
    }

    /**
     * Тест 3: Формирование отчёта по ставкам
     * Проверка производительности формирования отчёта по ставкам
     */
    @Test
    void testBetReportGeneration() {
        System.out.println("=== Тест 3: Формирование отчёта по ставкам ===");
        
        // Создание тестовых данных
        System.out.println("Создание тестовых данных...");
        List<Patient> patients = new ArrayList<>();
        List<Bet> bets = new ArrayList<>();
        
        // Создаём 100 пациентов
        for (int i = 0; i < 100; i++) {
            PatientRequest request = new PatientRequest();
            request.setFirstName("Report" + i);
            request.setLastName("Patient" + i);
            request.setDateOfBirth(LocalDate.now().minusYears(20 + (i % 50)));
            request.setGender(i % 2 == 0 ? "M" : "F");
            request.setInsuranceCompanyId(insuranceCompany.getId());
            Patient patient = patientService.createPatient(request, false);
            patients.add(patient);
        }
        
        // Создаём визиты и ставки
        Random random = new Random();
        // Счетчик визитов для каждого пациента для обеспечения уникальности дат
        java.util.Map<Integer, Integer> patientVisitCounters = new java.util.HashMap<>();
        for (int i = 0; i < 500; i++) {
            Patient patient = patients.get(random.nextInt(patients.size()));
            
            // Получаем счетчик визитов для этого пациента
            int visitCounter = patientVisitCounters.getOrDefault(patient.getId(), 0);
            patientVisitCounters.put(patient.getId(), visitCounter + 1);
            
            VisitRequest visitRequest = new VisitRequest();
            visitRequest.setPatientId(patient.getId());
            // Используем индекс цикла для гарантии уникальности даты
            visitRequest.setDateOfVisit(LocalDate.now().minusDays(365L + i));
            Visit visit = visitService.createVisit(visitRequest);
            visitService.updateVisitHDStatus(visit.getId(), VisitHDStatus.Accepted);
            
            BetRequest betRequest = new BetRequest();
            betRequest.setVisitId(visit.getId());
            betRequest.setDiagnosis("Report Diagnosis " + i);
            betRequest.setAmount((long) (100 + random.nextInt(900)));
            Bet bet = betService.createBet(betRequest);
            bets.add(bet);
        }
        
        // Тест формирования отчёта: получение всех ставок
        System.out.println("Формирование отчёта по ставкам...");
        long startTime = System.currentTimeMillis();
        List<BetPatientsResponse> report = betService.getAllBetPatients();
        long endTime = System.currentTimeMillis();
        long reportTime = endTime - startTime;
        
        System.out.println("Время формирования отчёта: " + reportTime + " мс");
        System.out.println("Записей в отчёте: " + report.size());
        
        // Проверка производительности
        assertTrue(reportTime < 5000, 
                "Формирование отчёта должно занимать ≤5 с. Фактическое время: " + reportTime + " мс");
        assertFalse(report.isEmpty(), "Отчёт должен содержать данные");
    }

    /**
     * Тест 4: Открытие карты пациента с 1,000 записей истории
     * Проверка производительности открытия карты пациента с большим количеством визитов
     */
    @Test
    void testPatientCardWithLargeHistory() {
        System.out.println("=== Тест 4: Открытие карты пациента с 1,000 записей истории ===");
        
        // Создание пациента
        PatientRequest patientRequest = new PatientRequest();
        patientRequest.setFirstName("History");
        patientRequest.setLastName("Patient");
        patientRequest.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patientRequest.setGender("M");
        patientRequest.setInsuranceCompanyId(insuranceCompany.getId());
        Patient patient = patientService.createPatient(patientRequest, false);
        
        // Создание 1,000 визитов для пациента
        System.out.println("Создание " + HISTORY_VISITS_PER_PATIENT + " визитов...");
        long startTime = System.currentTimeMillis();
        List<Visit> visits = new ArrayList<>();
        for (int i = 0; i < HISTORY_VISITS_PER_PATIENT; i++) {
            VisitRequest visitRequest = new VisitRequest();
            visitRequest.setPatientId(patient.getId());
            visitRequest.setDateOfVisit(LocalDate.now().minusDays(i));
            Visit visit = visitService.createVisit(visitRequest);
            visits.add(visit);
            
            if ((i + 1) % 100 == 0) {
                System.out.println("  Создано визитов: " + (i + 1));
            }
        }
        long creationTime = System.currentTimeMillis() - startTime;
        System.out.println("Время создания визитов: " + creationTime + " мс");
        
        // Тест открытия карты: получение всех визитов пациента
        System.out.println("Открытие карты пациента...");
        startTime = System.currentTimeMillis();
        List<Visit> patientVisits = visitService.getAllPatientVisits(patient.getId());
        long endTime = System.currentTimeMillis();
        long openTime = endTime - startTime;
        
        System.out.println("Время открытия карты: " + openTime + " мс");
        System.out.println("Количество визитов в истории: " + patientVisits.size());
        
        // Проверка производительности: открытие карты должно быть ≤5 с (требование TestPlan)
        assertTrue(openTime < 5000, 
                "Открытие карты пациента должно занимать ≤5 с. Фактическое время: " + openTime + " мс");
        assertEquals(HISTORY_VISITS_PER_PATIENT, patientVisits.size(), 
                "Должно быть получено " + HISTORY_VISITS_PER_PATIENT + " визитов");
    }

    /**
     * Тест 5: Измерение метрик производительности
     * Измерение потребления памяти, времени выполнения операций
     */
    @Test
    void testPerformanceMetrics() {
        System.out.println("=== Тест 5: Измерение метрик производительности ===");
        
        // Создание тестовых данных
        System.out.println("Создание тестовых данных (500 пациентов)...");
        List<Patient> patients = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            PatientRequest request = new PatientRequest();
            request.setFirstName("Metric" + i);
            request.setLastName("Test" + i);
            request.setDateOfBirth(LocalDate.now().minusYears(20 + (i % 50)));
            request.setGender(i % 2 == 0 ? "M" : "F");
            request.setInsuranceCompanyId(insuranceCompany.getId());
            Patient patient = patientService.createPatient(request, false);
            patients.add(patient);
        }
        
        // Измерение памяти
        long memoryBefore = getUsedMemory();
        System.out.println("Память до операций: " + formatBytes(memoryBefore));
        
        // Измерение времени получения всех пациентов
        long startTime = System.currentTimeMillis();
        List<Patient> allPatients = patientService.getAllPatients();
        long endTime = System.currentTimeMillis();
        long retrievalTime = endTime - startTime;
        
        long memoryAfter = getUsedMemory();
        long memoryUsed = memoryAfter - memoryBefore;
        
        System.out.println("\n=== Метрики производительности ===");
        System.out.println("Время получения всех пациентов: " + retrievalTime + " мс");
        System.out.println("Память после операций: " + formatBytes(memoryAfter));
        System.out.println("Использовано памяти: " + formatBytes(memoryUsed));
        System.out.println("Количество пациентов: " + allPatients.size());
        
        // Проверка производительности
        assertTrue(retrievalTime < 5000, 
                "Получение пациентов должно занимать ≤5 с. Фактическое время: " + retrievalTime + " мс");
        assertEquals(500, allPatients.size(), "Должно быть получено 500 пациентов");
    }

    // Вспомогательные методы

    private long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
        if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)) + " MB";
        return (bytes / (1024 * 1024 * 1024)) + " GB";
    }
}
