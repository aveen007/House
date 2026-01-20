# Руководство по Function Testing (TestPlan 5.2.2)

## Что такое Function Testing

Function Testing проверяет соответствие реализованного поведения системы функциональным требованиям SRS (FR-01…FR-18) по каждому основному и альтернативным бизнес-потокам.

## Требования TestPlan 5.2.2

1. ✅ Выделить набор Use Cases (FR-01, FR-02, FR-03, FR-06-08, FR-09-11, FR-11/12)
2. ✅ Подготовить детализированные тест-кейсы (preconditions, steps, expected results)
3. ✅ Выполнить тесты автоматизировано через Unit-тесты (JUnit) и API-тесты
4. ✅ Проверить как основной, так и альтернативные потоки

## Как запустить Function Testing

### Вариант 1: Автоматический запуск всех тестов

```bash
cd /Users/daniel/Documents/programming/mpi_proj/House
./scripts/run-function-testing.sh
```

Этот скрипт:
- Проверяет доступность сервера
- Запускает все Unit тесты (JUnit)
- Запускает все API функциональные тесты
- Выводит сводку по покрытию Use Cases

### Вариант 2: Пошаговый запуск

#### Шаг 1: Убедитесь, что сервер запущен

```bash
# Проверка доступности сервера
curl http://localhost:9314/api/getInsuranceCompanies
```

Если сервер не запущен, запустите его:
```bash
cd /Users/daniel/Documents/programming/mpi_proj/House
mvn spring-boot:run
```

#### Шаг 2: Запустите Unit тесты

```bash
cd /Users/daniel/Documents/programming/mpi_proj/House
mvn test -Dtest=*ServiceTest
```

Это проверит:
- `PatientServiceTest` - 4 теста (FR-01, FR-03)
- `VisitServiceTest` - 3 теста (FR-02)
- `BetServiceTest` - 4 теста (FR-09, FR-11/12)

#### Шаг 3: Запустите API функциональные тесты

```bash
cd /Users/daniel/Documents/programming/mpi_proj/House
./scripts/api-functional-smoke.sh
```

Это проверит все Use Cases через HTTP API:
- FR-01: Регистрация пациента
- FR-02: Создание визита
- FR-03: Проверка страховки (через создание пациента)
- FR-06-08: Работа с контрактами
- FR-09: Создание ставки
- FR-11/12: Финализация ставки

## Покрытие Use Cases

### FR-01: Регистрация пациента
- ✅ Основной поток: Успешная регистрация
- ✅ Альтернативный: Несуществующая страховая компания
- ✅ Альтернативный: Дубликат пациента
- ✅ Альтернативный: Ошибка проверки страховки

**Тесты:**
- Unit: `PatientServiceTest.createPatient_success()`
- Unit: `PatientServiceTest.createPatient_unknownInsurance_throws()`
- Unit: `PatientServiceTest.createPatient_duplicate_throws()`
- Unit: `PatientServiceTest.createPatient_invalidInsuranceVerification_throws()`
- API: `api-functional-smoke.sh` шаг 2

### FR-02: Создание визита
- ✅ Основной поток: Успешное создание визита
- ✅ Альтернативный: Несуществующий пациент
- ✅ Альтернативный: Дубликат визита

**Тесты:**
- Unit: `VisitServiceTest.createVisit_setsAwaitingStatus()`
- Unit: `VisitServiceTest.createVisit_patientNotFound_throws()`
- Unit: `VisitServiceTest.createVisit_duplicate_throws()`
- API: `api-functional-smoke.sh` шаг 5

### FR-03: Проверка страховки
- ✅ Основной поток: Успешная проверка страховки
- ✅ Альтернативный: Ошибка проверки страховки

**Тесты:**
- Unit: `PatientServiceTest.createPatient_success()` (с verifyInsurance=true)
- Unit: `PatientServiceTest.createPatient_invalidInsuranceVerification_throws()`

### FR-06, FR-07, FR-08: Работа с контрактами
- ✅ Основной поток: Создание контракта
- ✅ Основной поток: Сохранение контракта
- ✅ Основной поток: Подписание контракта
- ✅ Альтернативный: Несуществующий пациент

**Тесты:**
- Integration: `BusinessCycleTest.testCompleteBusinessCycle()`
- API: `api-functional-smoke.sh` шаги 12-14

### FR-09: Создание ставки
- ✅ Основной поток: Успешное создание ставки
- ✅ Альтернативный: Несуществующий визит
- ✅ Альтернативный: Контракт не подписан (проверяется на уровне бизнес-логики)

**Тесты:**
- Unit: `BetServiceTest.createBet_success()`
- Unit: `BetServiceTest.createBet_visitNotFound_throws()`
- Integration: `BusinessCycleTest.testCompleteBusinessCycle()`
- API: `api-functional-smoke.sh` шаг 7

### FR-11, FR-12: Финализация ставки
- ✅ Основной поток: Успешная финализация ставки
- ✅ Альтернативный: Несуществующая ставка
- ✅ Альтернативный: Несуществующий визит

**Тесты:**
- Unit: `BetServiceTest.finalizeBet_success()`
- Unit: `BetServiceTest.finalizeBet_betNotFound_throws()`
- Unit: `BetServiceTest.finalizeBet_visitNotFound_throws()`
- API: `api-functional-smoke.sh` шаг 16

## Ожидаемые результаты

После успешного выполнения всех тестов вы должны увидеть:

```
========================================
Function Testing завершен успешно!
========================================

Результаты:
  - Unit Tests: Все пройдены
  - API Tests: Все пройдены
  - Use Cases покрыты: 6 (FR-01, FR-02, FR-03, FR-06-08, FR-09, FR-11/12)
  - Основные потоки: 8 тест-кейсов
  - Альтернативные потоки: 10 тест-кейсов
```

## Документация

- **Детализированные тест-кейсы**: `FUNCTION_TEST_CASES.md`
- **Сводка**: `FUNCTION_TESTING_SUMMARY.md`
- **Отчет**: `TestResults.md` (раздел 7.1.2)

## Устранение проблем

### Проблема: Сервер не запущен
**Решение**: Запустите сервер командой `mvn spring-boot:run` в отдельном терминале

### Проблема: Ошибка аутентификации
**Решение**: Убедитесь, что в БД есть пользователь admin/password, или измените AUTH_USER и AUTH_PASS в скрипте

### Проблема: Ошибка "patient already exists"
**Решение**: Это нормально для повторных запусков. Скрипт использует timestamp для уникальности имен

### Проблема: Unit тесты падают
**Решение**: Убедитесь, что все зависимости установлены: `mvn clean install`

