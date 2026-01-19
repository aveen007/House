# Список команд для запуска всех видов тестирования

## Предварительные требования

1. **Сервер должен быть запущен** на порту 9314
2. **База данных PostgreSQL** должна быть доступна
3. **Перейти в правильную директорию** перед запуском команд

---

## 1. Unit Tests (JUnit) - 5.2.1

**Описание**: Unit тесты для сервисов и контроллеров

**Команда**:
```bash
cd /Users/daniel/Documents/programming/mpi_proj/House
mvn clean test
```

**Что тестируется**:
- PatientService
- VisitService
- PatientController

**Время выполнения**: ~30 секунд

---

## 2. Volume Testing - 5.2.8

**Описание**: Тестирование системы с большим объемом данных

**Команда**:
```bash
cd /Users/daniel/Documents/programming/mpi_proj/House
mvn -Dtest=VolumeTestingTest test
```

**Что тестируется**:
- Создание 1000 пациентов и визитов
- Производительность выборки
- Целостность данных

**Время выполнения**: ~2-3 минуты

---

## 3. Business Cycle Testing - 5.2.3

**Описание**: End-to-end тестирование полного бизнес-цикла

**Команда**:
```bash
cd /Users/daniel/Documents/programming/mpi_proj/House
mvn -Dtest=BusinessCycleTest test
```

**Что тестируется**:
- Регистрация пациента
- Создание визита
- Создание ставки
- Создание анализа
- Обновление статусов

**Время выполнения**: ~1 минута

---

## 4. API Functional Smoke Testing - 5.2.2

**Описание**: Функциональное тестирование API через HTTP запросы

**Команда**:
```bash
cd /Users/daniel/Documents/programming/mpi_proj/House
./scripts/api-functional-smoke.sh
```

**Или с параметрами**:
```bash
cd /Users/daniel/Documents/programming/mpi_proj/House
BASE_URL=http://localhost:9314 ./scripts/api-functional-smoke.sh
```

**Что тестируется**:
- FR-01: Регистрация пациента
- FR-02: Создание визита
- FR-03: Создание ставки
- FR-06-09: Работа с контрактами
- FR-13-16: Работа с анализами

**Время выполнения**: ~1-2 минуты

**Требования**: Сервер должен быть запущен

---

## 5. Database Integrity Testing - 5.2.1

**Описание**: Проверка целостности данных в БД

**Команда**:
```bash
cd /Users/daniel/Documents/programming/mpi_proj/House
./scripts/run-db-integrity-checks.sh
```

**Что тестируется**:
- NOT NULL constraints
- Foreign key relationships
- Domain value checks
- Data consistency

**Время выполнения**: ~10 секунд

**Требования**: PostgreSQL должен быть запущен в Docker

---

## 6. Security & Access Control Testing - 5.2.9

**Описание**: Тестирование безопасности и контроля доступа

**Команда**:
```bash
cd /Users/daniel/Documents/programming/mpi_proj/House
./scripts/security-access-tests.sh
```

**Или с параметрами**:
```bash
cd /Users/daniel/Documents/programming/mpi_proj/House
BASE_URL=http://localhost:9314 ./scripts/security-access-tests.sh
```

**Что тестируется**:
- Аутентификация
- Авторизация по ролям (ADMIN, STAFF, DOCTOR, HEAD_DOCTOR, LAWYER, PATIENT)
- CSRF защита
- SQL Injection / XSS защита

**Время выполнения**: ~2-3 минуты

**Требования**: Сервер должен быть запущен

---

## 7. Performance Profiling - 5.2.5

**Описание**: Профилирование производительности ключевых операций

**Команда**:
```bash
cd /Users/daniel/Documents/programming/mpi_proj/House
./scripts/performance-profiling.sh
```

**Или с параметрами**:
```bash
cd /Users/daniel/Documents/programming/mpi_proj/House
BASE_URL=http://localhost:9314 ITERATIONS=30 ./scripts/performance-profiling.sh
```

**Что тестируется**:
- Загрузка списка пациентов
- Открытие карты пациента
- Поиск по имени
- Создание ставки

**Метрики**:
- Mean, Median, Min, Max
- p90, p95
- Процент операций ≤5 секунд (PR-03)

**Время выполнения**: ~2-3 минуты

**Требования**: Сервер должен быть запущен

---

## 8. Load Testing (JMeter) - 5.2.6

**Описание**: Нагрузочное тестирование с 50 пользователями

**Команда**:
```bash
cd /Users/daniel/Documents/programming/mpi_proj/House/jmeter-tests
./run_load_test.sh 50
```

**Или через JMeter CLI напрямую**:
```bash
cd /Users/daniel/Documents/programming/mpi_proj/House/jmeter-tests
jmeter -n -t load-test/load_test_plan.jmx \
  -l results/load_test_50_users_$(date +%Y%m%d_%H%M%S).jtl \
  -e -o results/load_test_50_users_report_$(date +%Y%m%d_%H%M%S)/
```

**Что тестируется**:
- 50 одновременных пользователей
- Распределение: 20/15/10/5 (View/Manage/Bets/External)
- Ramp-up: 5 минут
- Hold: 30 минут

**Время выполнения**: ~35 минут

**Требования**: 
- Сервер должен быть запущен
- JMeter должен быть установлен

---

## 9. Stress Testing (JMeter) - 5.2.7

**Описание**: Стресс-тестирование с пошаговым увеличением нагрузки

**Команда**:
```bash
cd /Users/daniel/Documents/programming/mpi_proj/House/jmeter-tests
./run_stress_test.sh
```

**Что тестируется**:
- Пошаговое увеличение: 50 → 75 → 100 → 125 → 150 → 200 пользователей
- Каждый шаг: ramp-up 3 мин, hold 10 мин
- Определение точки деградации

**Время выполнения**: ~78 минут (6 шагов × 13 минут)

**Требования**: 
- Сервер должен быть запущен
- JMeter должен быть установлен
- Достаточно ресурсов системы


