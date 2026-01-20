-- Скрипт для наполнения БД большими объёмами данных для Volume Testing
-- Согласно TestPlan 5.2.8:
-- - 10,000 пациентов (~50 MB)
-- - 50,000 ставок (~150 MB)
-- - 100,000 лабораторных анализов (~150 MB)

-- ВНИМАНИЕ: Этот скрипт создаёт большие объёмы данных и может занять значительное время

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
    analysis_patient_id INTEGER;
    analysis_bet_id INTEGER;
    created_analysis_id INTEGER;
BEGIN
    RAISE NOTICE '=== Начало наполнения БД для Volume Testing ===';
    start_time := clock_timestamp();
    
    -- Получение или создание страховой компании
    SELECT id INTO insurance_company_id 
    FROM insurance_company 
    LIMIT 1;
    
    IF insurance_company_id IS NULL THEN
        INSERT INTO insurance_company (company_name, api_url, api_key)
        VALUES ('Volume Test Insurance', 'http://test-api.com', 'test-key')
        RETURNING id INTO insurance_company_id;
        RAISE NOTICE 'Создана страховая компания с ID: %', insurance_company_id;
    ELSE
        RAISE NOTICE 'Используется существующая страховая компания с ID: %', insurance_company_id;
    END IF;
    
    -- Получение или создание типа анализа
    SELECT id INTO analysis_id 
    FROM analysis 
    LIMIT 1;
    
    IF analysis_id IS NULL THEN
        INSERT INTO analysis (title)
        VALUES ('Blood Test')
        RETURNING id INTO analysis_id;
        RAISE NOTICE 'Создан тип анализа с ID: %', analysis_id;
    ELSE
        RAISE NOTICE 'Используется существующий тип анализа с ID: %', analysis_id;
    END IF;
    
    -- 1. Создание 10,000 пациентов
    RAISE NOTICE '=== Создание 10,000 пациентов ===';
    FOR i IN 1..10000 LOOP
        INSERT INTO patient (
            first_name, 
            last_name, 
            date_of_birth, 
            gender, 
            insurance_company_id
        ) VALUES (
            'Patient' || i,
            'Test' || i,
            CURRENT_DATE - INTERVAL '20 years' - (INTERVAL '1 year' * (i % 50)),
            CASE WHEN i % 2 = 0 THEN 'M' ELSE 'F' END,
            insurance_company_id
        ) RETURNING id INTO patient_id;
        
        IF i % 1000 = 0 THEN
            RAISE NOTICE 'Создано пациентов: %', i;
        END IF;
    END LOOP;
    
    RAISE NOTICE 'Создание пациентов завершено';
    
    -- 2. Создание визитов и 50,000 ставок
    RAISE NOTICE '=== Создание визитов и 50,000 ставок ===';
    FOR i IN 1..50000 LOOP
        -- Выбираем случайного пациента (ID от 1 до 10000)
        patient_id := 1 + (i % 10000);
        
        -- Создаём визит
        INSERT INTO visit (
            patient_id,
            date_of_visit,
            hd_status
        ) VALUES (
            patient_id,
            CURRENT_DATE - (random() * 365)::INTEGER,
            'Accepted'
        ) RETURNING id INTO visit_id;
        
        -- Создаём ставку
        INSERT INTO bet (
            visit_id,
            diagnosis,
            amount
        ) VALUES (
            visit_id,
            'Diagnosis ' || i,
            (100 + (random() * 900)::INTEGER)::BIGINT
        ) RETURNING bet_id INTO bet_id;
        
        IF i % 5000 = 0 THEN
            RAISE NOTICE 'Создано ставок: %', i;
        END IF;
    END LOOP;
    
    RAISE NOTICE 'Создание ставок завершено';
    
    -- 3. Создание 100,000 анализов
    RAISE NOTICE '=== Создание 100,000 анализов ===';
    FOR i IN 1..100000 LOOP
        -- Выбираем случайного пациента
        analysis_patient_id := 1 + (i % 10000);
        
        -- Получаем случайную ставку
        SELECT bet_id INTO analysis_bet_id FROM bet ORDER BY RANDOM() LIMIT 1;
        
        IF analysis_bet_id IS NOT NULL THEN
            -- Создаём анализ пациента
            INSERT INTO patient_analysis (
                patient_id,
                analysis_id,
                date,
                status
            ) VALUES (
                analysis_patient_id,
                analysis_id,
                CURRENT_DATE - (random() * 365)::INTEGER,
                'AwaitingHD'
            ) RETURNING id INTO created_analysis_id;
            
            -- Создаём связь анализ-ставка
            INSERT INTO analysis_bet (
                bet_id,
                patient_analysis_id
            ) VALUES (
                analysis_bet_id,
                created_analysis_id
            );
        END IF;
        
        IF i % 10000 = 0 THEN
            RAISE NOTICE 'Создано анализов: %', i;
        END IF;
    END LOOP;
    
    RAISE NOTICE 'Создание анализов завершено';
    
    end_time := clock_timestamp();
    duration := end_time - start_time;
    
    RAISE NOTICE '=== Наполнение БД завершено ===';
    RAISE NOTICE 'Общее время выполнения: %', duration;
    RAISE NOTICE 'Создано пациентов: 10,000';
    RAISE NOTICE 'Создано ставок: 50,000';
    RAISE NOTICE 'Создано анализов: 100,000';
    
    -- Вывод статистики
    RAISE NOTICE '=== Статистика БД ===';
    RAISE NOTICE 'Всего пациентов: %', (SELECT COUNT(*) FROM patient);
    RAISE NOTICE 'Всего визитов: %', (SELECT COUNT(*) FROM visit);
    RAISE NOTICE 'Всего ставок: %', (SELECT COUNT(*) FROM bet);
    RAISE NOTICE 'Всего анализов: %', (SELECT COUNT(*) FROM patient_analysis);
    
END $$;

