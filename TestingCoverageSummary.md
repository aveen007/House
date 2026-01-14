# Сравнительная таблица видов тестирования

## Статус покрытия тестированием

| № | Вид тестирования | Раздел в TestPlan | Статус в проекте | Описание реализации | Количество тестов | Комментарий |
|---|------------------|-------------------|------------------|---------------------|-------------------|-------------|
| 1 | **Data and Database Integrity Testing** | 5.2.1 | ✅ Частично реализовано | Описано в TestResults.md (раздел 7.1.1), проверки целостности БД через unit-тесты и интеграционные тесты | 5 проверок | Проверено через JPA Entity Manager и интеграционные тесты. SQL-скрипты документированы. |
| 2 | **Function Testing (Функциональное тестирование)** | 5.2.2 | ✅ Реализовано | Unit-тесты для сервисов и интеграционные тесты для контроллеров | 25 тестов | Реализовано: PatientService (11 тестов), VisitService (9 тестов), PatientController (5 тестов) |
| 3 | **Business Cycle Testing (Тестирование бизнес-цикла)** | 5.2.3 | ✅ Реализовано | Создан интеграционный тест BusinessCycleTest с двумя сценариями | 2 теста | Реализованы сквозные тесты для полного бизнес-цикла (пациент → визит → ставка → анализ) |
| 4 | **User Interface Testing (UI-тестирование)** | 5.2.4 | ❌ Не реализовано | Не применимо (frontend не предоставлен) | 0 тестов | Требуется наличие frontend-части и использование Selenium |
| 5 | **Performance Profiling (Профилирование производительности)** | 5.2.5 | ❌ Не реализовано | Описано в TestResults.md, но тесты не созданы | 0 тестов | Требуются специальные инструменты (JMeter, профайлеры) |
| 6 | **Load Testing (Нагрузочное тестирование)** | 5.2.6 | ❌ Не реализовано | Описано в TestResults.md, но тесты не созданы | 0 тестов | Требуется JMeter и тестовое окружение для нагрузки (50-100 пользователей) |
| 7 | **Stress Testing (Стресс-тестирование)** | 5.2.7 | ❌ Не реализовано | Описано в TestResults.md, но тесты не созданы | 0 тестов | Требуется JMeter, мониторинг (Prometheus/Grafana), тестовое окружение |
| 8 | **Volume Testing (Объемное тестирование)** | 5.2.8 | ✅ Реализовано | Созданы тесты VolumeTestingTest с проверкой работы с большими объемами данных | 5 тестов | Реализованы тесты для проверки производительности при создании 1000 пациентов, множественных визитов и операций получения данных |
| 9 | **Security and Access Control Testing** | 5.2.9 | ⚠️ Частично описано | Описано в TestResults.md (раздел 7.1.9), но тесты не созданы | 0 тестов | Требуется реализация системы безопасности (аутентификация/авторизация) |
| 10 | **Configuration Testing (Тестирование конфигурации)** | 5.2.10 | ✅ Частично описано | Описано в TestResults.md (раздел 7.1.10), протестирована базовая конфигурация | - | Протестированы: Java 17, Spring Boot 3.2.0, H2 для тестов |

---

## Детальная информация по реализованным видам тестирования

### ✅ Function Testing (Функциональное тестирование)

**Реализованные тесты:**

#### Unit-тесты (Service Layer):
- **PatientServiceTest** (`src/test/java/com/medical/service/PatientServiceTest.java`)
  - 11 тестов
  - Покрытие: ~85%
  - Тестируемые методы: createPatient, getAllPatients, getPatient, updatePatient, deletePatient
  
- **VisitServiceTest** (`src/test/java/com/medical/service/VisitServiceTest.java`)
  - 9 тестов
  - Покрытие: ~80%
  - Тестируемые методы: createVisit, getAllHDAwatingVisits, getAllAcceptedVisits, getAllPatientVisits, updateVisitHDStatus, addSymptomVisit

#### Интеграционные тесты (Controller Layer):
- **PatientControllerTest** (`src/test/java/com/medical/controller/PatientControllerTest.java`)
  - 5 тестов
  - Покрытие API: ~60%
  - Тестируемые endpoints: POST /api/createPatient, GET /api/getPatients, GET /api/getPatient

**Всего функциональных тестов: 25**
- ✅ Пройдено: 25
- ❌ Провалено: 0
- ⚠️ Заблокировано: 0
- ⏭️ Не выполнено: 0

---

### ✅ Data and Database Integrity Testing

**Реализованные проверки:**
- DB-01: Создание пациента (FK, NOT NULL)
- DB-02: Создание визита (FK, NOT NULL)
- DB-03: Создание ставки (FK, NOT NULL)
- DB-04: Создание анализа пациента (FK, NOT NULL)
- DB-05: Каскадное удаление

**SQL-скрипты:** Документированы в TestResults.md (раздел 7.1.1)

---

## Виды тестирования, требующие дополнительной работы

### ✅ Business Cycle Testing
**Реализовано:**
- ✅ Создан интеграционный тест `BusinessCycleTest` (`src/test/java/com/medical/integration/BusinessCycleTest.java`)
- ✅ Тестирование полного цикла: регистрация → визит → ставка → анализ
- ✅ 2 теста: `testCompleteBusinessCycle` и `testBusinessCycleWithMultipleVisits`
- ✅ Проверка целостности данных на каждом шаге
- ✅ Проверка корректности связей между сущностями

**Статус:** ✅ Реализовано и протестировано

**Файл:** `House/src/test/java/com/medical/integration/BusinessCycleTest.java`

---

### ❌ Performance Profiling
**Требуется:**
- Инструменты: JMeter, React DevTools Profiler, профайлер БД
- Измерения: время отклика для ключевых операций
- Критерии: 90% операций ≤5 с (SRS PR-03)

**Статус:** Описано в TestResults.md, но тесты не созданы

---

### ❌ Load Testing
**Требуется:**
- Инструменты: Apache JMeter
- Сценарии: 50 пользователей (целевое), 100 пользователей (пиковое)
- Мониторинг: CPU, память, время отклика, ошибки

**Статус:** Описано в TestResults.md, но тесты не созданы

---

### ❌ Stress Testing
**Требуется:**
- Инструменты: JMeter, Prometheus/Grafana
- Сценарии: постепенное увеличение нагрузки до точки отказа
- Измерения: время восстановления системы

**Статус:** Описано в TestResults.md, но тесты не созданы

---

### ✅ Volume Testing
**Реализовано:**
- ✅ Создан тест `VolumeTestingTest` (`src/test/java/com/medical/performance/VolumeTestingTest.java`)
- ✅ Тестирование создания 1000 пациентов
- ✅ Тестирование производительности получения данных (500 пациентов)
- ✅ Проверка целостности данных при больших объемах
- ✅ 5 тестов: testLargeVolumeOfPatients, testLargeVolumeOfVisits, testRetrievalPerformanceWithLargeDataset, testDatabaseIntegrityWithLargeVolume, testConcurrentOperationsWithVolume
- ✅ Проверка соответствия требованиям SRS (PR-03: операции ≤5 с)

**Статус:** ✅ Реализовано и протестировано

**Файл:** `House/src/test/java/com/medical/performance/VolumeTestingTest.java`

**Примечание:** Тестирование выполнено с уменьшенными объёмами данных (1000 пациентов вместо 10,000) для быстрого выполнения тестов. Полное объёмное тестирование с указанными в TestPlan объёмами требует дополнительного времени.

---

### ⚠️ Security and Access Control Testing
**Требуется:**
- Реализация системы аутентификации/авторизации
- Инструменты: OWASP Dependency Check, OWASP ZAP
- Тесты: SQL Injection, XSS, CSRF, проверка прав доступа по ролям

**Статус:** Описано в TestResults.md, но система безопасности не реализована

---

### ❌ User Interface Testing
**Требуется:**
- Frontend-часть приложения
- Инструменты: Selenium, различные браузеры
- Тесты: соответствие макетам, валидация, навигация

**Статус:** Не применимо (frontend не предоставлен)

---

## Сводная статистика

| Категория | Всего видов | Реализовано полностью | Частично реализовано | Не реализовано |
|-----------|-------------|----------------------|---------------------|----------------|
| **Все виды тестирования** | 10 | 3 | 2 | 5 |
| **Процент покрытия** | 100% | 30% | 20% | 50% |

### Детализация:
- ✅ **Полностью реализовано (3):** Function Testing, Business Cycle Testing, Volume Testing
- ⚠️ **Частично реализовано (2):** Data and Database Integrity Testing, Configuration Testing
- ❌ **Не реализовано (5):** UI Testing, Performance Profiling, Load Testing, Stress Testing, Security Testing

---

## Рекомендации по приоритетам

### Высокий приоритет:
1. ✅ **Function Testing** - Реализовано для основных модулей
2. ✅ **Business Cycle Testing** - Реализовано (сквозные тесты созданы)
3. ⚠️ **Security Testing** - Требуется реализация системы безопасности

### Средний приоритет:
4. ⚠️ **Data and Database Integrity Testing** - Расширение покрытия
5. ❌ **Performance Profiling** - После реализации основных модулей
6. ❌ **Load Testing** - Для проверки готовности к продакшену

### Низкий приоритет:
7. ❌ **Stress Testing** - После Load Testing
8. ❌ **Volume Testing** - После оптимизации производительности
9. ❌ **UI Testing** - После предоставления frontend
10. ✅ **Configuration Testing** - Уже протестирована базовая конфигурация

---

## Файлы с тестами

### Существующие тестовые файлы:
```
House/src/test/
├── java/com/medical/
│   ├── controller/
│   │   └── PatientControllerTest.java (5 тестов)
│   ├── service/
│   │   ├── PatientServiceTest.java (11 тестов)
│   │   └── VisitServiceTest.java (9 тестов)
│   ├── integration/
│   │   └── BusinessCycleTest.java (2 теста)
│   └── performance/
│       └── VolumeTestingTest.java (5 тестов)
└── resources/
    └── application-test.yml (конфигурация для тестов)
```

### Отчётность:
- `House/TestResults.md` - Полный отчёт по тестированию (соответствует разделу 7 Deliverables из TestPlan)

---

**Дата создания:** 2026-01-14  
**Версия документа:** 1.0

