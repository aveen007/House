-- Скрипт для подготовки БД перед Load Testing
-- Очищает таблицы и заполняет их небольшим количеством данных (до 100 записей)
-- для демонстрации нормальной нагрузки в JMeter

DO $$
DECLARE
    insurance_company_id INTEGER;
    analysis_id INTEGER;
    patient_id INTEGER;
    visit_id INTEGER;
    bet_id INTEGER;
    i INTEGER;
    start_time TIMESTAMP;
    end_time TIMESTAMP;
    duration INTERVAL;
    deleted_count INTEGER;
BEGIN
    RAISE NOTICE '=== Подготовка БД для Load Testing ===';
    start_time := clock_timestamp();
    
    -- Шаг 1: Очистка таблиц в правильном порядке (с учетом Foreign Key constraints)
    RAISE NOTICE 'Шаг 1: Очистка таблиц...';
    
    -- Удаление зависимых записей
    DELETE FROM analysis_bet;
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RAISE NOTICE '  Удалено из analysis_bet: %', deleted_count;
    
    DELETE FROM analysis_result;
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RAISE NOTICE '  Удалено из analysis_result: %', deleted_count;
    
    DELETE FROM patient_analysis;
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RAISE NOTICE '  Удалено из patient_analysis: %', deleted_count;
    
    DELETE FROM fin_bet;
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RAISE NOTICE '  Удалено из fin_bet: %', deleted_count;
    
    DELETE FROM bets;
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RAISE NOTICE '  Удалено из bets: %', deleted_count;
    
    DELETE FROM visits;
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RAISE NOTICE '  Удалено из visits: %', deleted_count;
    
    DELETE FROM contracts;
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RAISE NOTICE '  Удалено из contracts: %', deleted_count;
    
    DELETE FROM patients;
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RAISE NOTICE '  Удалено из patients: %', deleted_count;
    
    RAISE NOTICE 'Очистка завершена';
    
    -- Шаг 2: Получение или создание страховой компании
    RAISE NOTICE 'Шаг 2: Подготовка страховой компании...';
    SELECT ic.insurance_company_id INTO insurance_company_id 
    FROM insurance_companies ic
    LIMIT 1;
    
    IF insurance_company_id IS NULL THEN
        INSERT INTO insurance_companies (company_name, api_url, api_key)
        VALUES ('Load Test Insurance', 'http://test-api.com', 'test-key')
        RETURNING insurance_company_id INTO insurance_company_id;
        RAISE NOTICE '  Создана страховая компания с ID: %', insurance_company_id;
    ELSE
        RAISE NOTICE '  Используется существующая страховая компания с ID: %', insurance_company_id;
    END IF;
    
    -- Шаг 3: Получение или создание типа анализа
    RAISE NOTICE 'Шаг 3: Подготовка типа анализа...';
    SELECT id INTO analysis_id 
    FROM analysis 
    LIMIT 1;
    
    IF analysis_id IS NULL THEN
        INSERT INTO analysis (title)
        VALUES ('Blood Test')
        RETURNING id INTO analysis_id;
        RAISE NOTICE '  Создан тип анализа с ID: %', analysis_id;
    ELSE
        RAISE NOTICE '  Используется существующий тип анализа с ID: %', analysis_id;
    END IF;
    
    -- Шаг 4: Создание 100 пациентов
    RAISE NOTICE 'Шаг 4: Создание 100 пациентов...';
    FOR i IN 1..100 LOOP
        INSERT INTO patients (
            first_name, 
            last_name, 
            date_of_birth, 
            gender, 
            insurance_company_id
        )
        VALUES (
            'LoadTestPatient' || i,
            'User' || i,
            CURRENT_DATE - INTERVAL '30 years' - (INTERVAL '1 day' * (i % 365)),
            CASE WHEN i % 2 = 0 THEN 'M' ELSE 'F' END,
            insurance_company_id
        )
        RETURNING patients.patient_id INTO patient_id;
        
        -- Создание визита для каждого пациента
        INSERT INTO visits (
            patient_id,
            date_of_visit
        )
        VALUES (
            patient_id,
            CURRENT_DATE - (INTERVAL '1 day' * (i % 30))
        )
        RETURNING visits.visit_id INTO visit_id;
        
        -- Создание ставки для каждого визита
        INSERT INTO bets (
            visit_id,
            diagnosis,
            amount
        )
        VALUES (
            visit_id,
            'Load Test Diagnosis ' || i,
            100 + (i * 10)
        )
        RETURNING bets.bet_id INTO bet_id;
        
        -- Прогресс каждые 10 записей
        IF i % 10 = 0 THEN
            RAISE NOTICE '  Создано пациентов: %', i;
        END IF;
    END LOOP;
    
    RAISE NOTICE 'Создание данных завершено';
    
    -- Шаг 5: Финальная статистика
    RAISE NOTICE 'Шаг 5: Финальная статистика...';
    SELECT COUNT(*) INTO deleted_count FROM patients;
    RAISE NOTICE '  Пациенты: %', deleted_count;
    
    SELECT COUNT(*) INTO deleted_count FROM visits;
    RAISE NOTICE '  Визиты: %', deleted_count;
    
    SELECT COUNT(*) INTO deleted_count FROM bets;
    RAISE NOTICE '  Ставки: %', deleted_count;
    
    end_time := clock_timestamp();
    duration := end_time - start_time;
    RAISE NOTICE '=== Подготовка БД завершена за % ===', duration;
    
END $$;

