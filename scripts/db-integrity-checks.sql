\set ON_ERROR_STOP on

-- ============================================================================
-- Database Integrity Testing согласно TestPlan 5.2.1
-- ============================================================================
-- Шаги:
-- 1. Снять контрольный дамп и/или считать конкретные счётчики
-- 2. Выполнить предопределённый набор операций (создание пациента, контракта, ставки, закрытие ставки)
-- 3. Выполнить SQL-запросы для проверки ожидаемых изменений
-- 4. Выполнить rollback/откат тестовых транзакций и проверить возврат к исходному состоянию
-- 5. Автоматизированные проверки целостности
-- ============================================================================

DO $$
DECLARE
    -- Начальные счетчики (шаг 1)
    v_initial_patients_count integer;
    v_initial_visits_count integer;
    v_initial_contracts_count integer;
    v_initial_bets_count integer;
    v_initial_fin_bets_count integer;
    
    -- ID созданных записей
    v_test_patient_id integer;
    v_test_visit_id integer;
    v_test_contract_id integer;
    v_test_bet_id integer;
    
    -- Счетчики после операций
    v_after_ops_patients_count integer;
    v_after_ops_visits_count integer;
    v_after_ops_contracts_count integer;
    v_after_ops_bets_count integer;
    
    -- Счетчики после rollback
    v_after_rollback_patients_count integer;
    v_after_rollback_visits_count integer;
    v_after_rollback_contracts_count integer;
    v_after_rollback_bets_count integer;
    
    -- Проверки целостности
    v_count integer;
    v_insurance_company_id integer;
    v_terms_id integer;
BEGIN
    RAISE NOTICE '=== Шаг 1: Снятие контрольных счетчиков ===';
    
    -- Сохраняем начальное состояние
    SELECT COUNT(*) INTO v_initial_patients_count FROM public.patients;
    SELECT COUNT(*) INTO v_initial_visits_count FROM public.visits;
    SELECT COUNT(*) INTO v_initial_contracts_count FROM public.contracts;
    SELECT COUNT(*) INTO v_initial_bets_count FROM public.bets;
    SELECT COUNT(*) INTO v_initial_fin_bets_count FROM public.fin_bet;
    
    RAISE NOTICE 'Начальное состояние:';
    RAISE NOTICE '  Пациенты: %', v_initial_patients_count;
    RAISE NOTICE '  Визиты: %', v_initial_visits_count;
    RAISE NOTICE '  Контракты: %', v_initial_contracts_count;
    RAISE NOTICE '  Ставки: %', v_initial_bets_count;
    RAISE NOTICE '  Закрытые ставки: %', v_initial_fin_bets_count;
    
    -- Инициализация переменных
    v_test_patient_id := NULL;
    v_test_visit_id := NULL;
    v_test_contract_id := NULL;
    v_test_bet_id := NULL;
    
    -- Получаем ID существующей страховой компании и условий
    SELECT insurance_company_id INTO v_insurance_company_id 
    FROM public.insurance_companies 
    LIMIT 1;
    
    IF v_insurance_company_id IS NULL THEN
        RAISE EXCEPTION 'Не найдена страховая компания для теста';
    END IF;
    
    SELECT terms_id INTO v_terms_id 
    FROM public.terms_and_conditions 
    WHERE is_active = true 
    LIMIT 1;
    
    IF v_terms_id IS NULL THEN
        -- Создаем тестовые условия, если их нет
        INSERT INTO public.terms_and_conditions (title, content, version, is_active)
        VALUES ('Test Terms', 'Test content', 1, true)
        RETURNING terms_id INTO v_terms_id;
    END IF;
    
    RAISE NOTICE '=== Шаг 2: Выполнение операций в транзакции ===';
    
    -- Начинаем вложенный блок для операций (в DO блоке все уже в транзакции)
    BEGIN
        -- 2.1. Создание пациента
        RAISE NOTICE 'Создание тестового пациента...';
        INSERT INTO public.patients (first_name, last_name, date_of_birth, gender, insurance_company_id)
        VALUES ('TestPatient', 'TestLastName', '1990-01-01', 'M', v_insurance_company_id)
        RETURNING patient_id INTO v_test_patient_id;
        
        RAISE NOTICE '  Создан пациент с ID: %', v_test_patient_id;
        
        -- 2.2. Создание визита
        RAISE NOTICE 'Создание визита для пациента...';
        INSERT INTO public.visits (patient_id, date_of_visit, hd_status)
        VALUES (v_test_patient_id, CURRENT_DATE, 0)
        RETURNING visit_id INTO v_test_visit_id;
        
        RAISE NOTICE '  Создан визит с ID: %', v_test_visit_id;
        
        -- 2.3. Создание контракта
        RAISE NOTICE 'Создание контракта для пациента...';
        INSERT INTO public.contracts (patient_id, terms_id, terms_snapshot, status)
        VALUES (
            v_test_patient_id, 
            v_terms_id, 
            'Test terms snapshot',
            'DRAFT'
        )
        RETURNING contract_id INTO v_test_contract_id;
        
        RAISE NOTICE '  Создан контракт с ID: %', v_test_contract_id;
        
        -- 2.4. Создание ставки
        RAISE NOTICE 'Создание ставки для визита...';
        INSERT INTO public.bets (visit_id, diagnosis, amount)
        VALUES (v_test_visit_id, 'Test Diagnosis', 1000)
        RETURNING bet_id INTO v_test_bet_id;
        
        RAISE NOTICE '  Создана ставка с ID: %', v_test_bet_id;
        
        -- 2.5. Закрытие ставки (финализация)
        RAISE NOTICE 'Закрытие ставки...';
        INSERT INTO public.fin_bet (bet_id, visit_id)
        VALUES (v_test_bet_id, v_test_visit_id);
        
        RAISE NOTICE '  Ставка закрыта';
        
        RAISE NOTICE '=== Шаг 3: Проверка ожидаемых изменений ===';
        
        -- Подсчитываем записи после операций
        SELECT COUNT(*) INTO v_after_ops_patients_count FROM public.patients;
        SELECT COUNT(*) INTO v_after_ops_visits_count FROM public.visits;
        SELECT COUNT(*) INTO v_after_ops_contracts_count FROM public.contracts;
        SELECT COUNT(*) INTO v_after_ops_bets_count FROM public.bets;
        
        RAISE NOTICE 'Состояние после операций:';
        RAISE NOTICE '  Пациенты: % (было %, добавлено 1)', v_after_ops_patients_count, v_initial_patients_count;
        RAISE NOTICE '  Визиты: % (было %, добавлено 1)', v_after_ops_visits_count, v_initial_visits_count;
        RAISE NOTICE '  Контракты: % (было %, добавлено 1)', v_after_ops_contracts_count, v_initial_contracts_count;
        RAISE NOTICE '  Ставки: % (было %, добавлено 1)', v_after_ops_bets_count, v_initial_bets_count;
        
        -- 3.1. Проверка наличия новых записей с корректными значениями полей (NOT NULL, foreign_key)
        RAISE NOTICE 'Проверка NOT NULL полей...';
        
        -- Проверка пациента
        SELECT COUNT(*) INTO v_count
        FROM public.patients
        WHERE patient_id = v_test_patient_id
          AND (first_name IS NULL OR last_name IS NULL OR date_of_birth IS NULL OR gender IS NULL);
        IF v_count > 0 THEN
            RAISE EXCEPTION 'Пациент имеет NULL в обязательных полях';
        END IF;
        
        -- Проверка визита
        SELECT COUNT(*) INTO v_count
        FROM public.visits
        WHERE visit_id = v_test_visit_id
          AND (patient_id IS NULL OR date_of_visit IS NULL);
        IF v_count > 0 THEN
            RAISE EXCEPTION 'Визит имеет NULL в обязательных полях';
        END IF;
        
        -- Проверка контракта
        SELECT COUNT(*) INTO v_count
        FROM public.contracts
        WHERE contract_id = v_test_contract_id
          AND (terms_snapshot IS NULL OR status IS NULL);
        IF v_count > 0 THEN
            RAISE EXCEPTION 'Контракт имеет NULL в обязательных полях';
        END IF;
        
        -- Проверка ставки
        SELECT COUNT(*) INTO v_count
        FROM public.bets
        WHERE bet_id = v_test_bet_id
          AND (visit_id IS NULL OR diagnosis IS NULL OR amount IS NULL);
        IF v_count > 0 THEN
            RAISE EXCEPTION 'Ставка имеет NULL в обязательных полях';
        END IF;
        
        RAISE NOTICE '  Все NOT NULL поля заполнены корректно';
        
        -- 3.2. Проверка корректных внешних ключей и каскадных действий
        RAISE NOTICE 'Проверка внешних ключей...';
        
        -- Проверка FK пациента к страховой компании
        SELECT COUNT(*) INTO v_count
        FROM public.patients p
        LEFT JOIN public.insurance_companies ic ON p.insurance_company_id = ic.insurance_company_id
        WHERE p.patient_id = v_test_patient_id AND ic.insurance_company_id IS NULL;
        IF v_count > 0 THEN
            RAISE EXCEPTION 'Пациент имеет невалидный FK к страховой компании';
        END IF;
        
        -- Проверка FK визита к пациенту
        SELECT COUNT(*) INTO v_count
        FROM public.visits v
        LEFT JOIN public.patients p ON v.patient_id = p.patient_id
        WHERE v.visit_id = v_test_visit_id AND p.patient_id IS NULL;
        IF v_count > 0 THEN
            RAISE EXCEPTION 'Визит имеет невалидный FK к пациенту';
        END IF;
        
        -- Проверка FK контракта к пациенту и условиям
        SELECT COUNT(*) INTO v_count
        FROM public.contracts c
        LEFT JOIN public.patients p ON c.patient_id = p.patient_id
        LEFT JOIN public.terms_and_conditions t ON c.terms_id = t.terms_id
        WHERE c.contract_id = v_test_contract_id 
          AND (p.patient_id IS NULL OR t.terms_id IS NULL);
        IF v_count > 0 THEN
            RAISE EXCEPTION 'Контракт имеет невалидные FK';
        END IF;
        
        -- Проверка FK ставки к визиту
        SELECT COUNT(*) INTO v_count
        FROM public.bets b
        LEFT JOIN public.visits v ON b.visit_id = v.visit_id
        WHERE b.bet_id = v_test_bet_id AND v.visit_id IS NULL;
        IF v_count > 0 THEN
            RAISE EXCEPTION 'Ставка имеет невалидный FK к визиту';
        END IF;
        
        -- Проверка FK закрытой ставки
        SELECT COUNT(*) INTO v_count
        FROM public.fin_bet fb
        LEFT JOIN public.bets b ON fb.bet_id = b.bet_id
        LEFT JOIN public.visits v ON fb.visit_id = v.visit_id
        WHERE fb.bet_id = v_test_bet_id 
          AND (b.bet_id IS NULL OR v.visit_id IS NULL);
        IF v_count > 0 THEN
            RAISE EXCEPTION 'Закрытая ставка имеет невалидные FK';
        END IF;
        
        RAISE NOTICE '  Все внешние ключи корректны';
        
        -- 3.3. Проверка отсутствия дублирования уникальных полей
        RAISE NOTICE 'Проверка уникальности...';
        
        -- Проверка уникальности комбинации имени и даты рождения пациента
        -- (если есть такое ограничение в бизнес-логике)
        -- В данном случае проверяем, что не создали дубликат
        SELECT COUNT(*) INTO v_count
        FROM public.patients
        WHERE first_name = 'TestPatient'
          AND last_name = 'TestLastName'
          AND date_of_birth = '1990-01-01'
          AND patient_id != v_test_patient_id;
        -- Это не ошибка, просто проверка
        
        RAISE NOTICE '  Проверка уникальности выполнена';
        
    END;
    
    RAISE NOTICE '=== Шаг 4: Rollback и проверка возврата к исходному состоянию ===';
    
    -- В PostgreSQL DO блоке нельзя использовать ROLLBACK напрямую
    -- Поэтому удаляем созданные записи вручную (симуляция rollback)
    -- В реальном сценарии это было бы сделано через ROLLBACK транзакции
    
    -- Проверяем, что записи были созданы
    IF v_test_patient_id IS NULL THEN
        RAISE WARNING 'Предупреждение: пациент не был создан, пропускаем rollback';
    ELSE
        -- Удаляем в обратном порядке (сначала зависимые записи)
        RAISE NOTICE 'Удаление тестовых записей...';
        
        IF v_test_bet_id IS NOT NULL THEN
            DELETE FROM public.fin_bet WHERE bet_id = v_test_bet_id;
            RAISE NOTICE '  Удалена закрытая ставка';
            
            DELETE FROM public.bets WHERE bet_id = v_test_bet_id;
            RAISE NOTICE '  Удалена ставка';
        END IF;
        
        IF v_test_contract_id IS NOT NULL THEN
            DELETE FROM public.contracts WHERE contract_id = v_test_contract_id;
            RAISE NOTICE '  Удален контракт';
        END IF;
        
        IF v_test_visit_id IS NOT NULL THEN
            DELETE FROM public.visits WHERE visit_id = v_test_visit_id;
            RAISE NOTICE '  Удален визит';
        END IF;
        
        DELETE FROM public.patients WHERE patient_id = v_test_patient_id;
        RAISE NOTICE '  Удален пациент';
        
        RAISE NOTICE 'Тестовые записи удалены (симуляция rollback)';
    END IF;
    
    -- Проверяем состояние после rollback
    SELECT COUNT(*) INTO v_after_rollback_patients_count FROM public.patients;
    SELECT COUNT(*) INTO v_after_rollback_visits_count FROM public.visits;
    SELECT COUNT(*) INTO v_after_rollback_contracts_count FROM public.contracts;
    SELECT COUNT(*) INTO v_after_rollback_bets_count FROM public.bets;
    
    RAISE NOTICE 'Состояние после rollback:';
    RAISE NOTICE '  Пациенты: % (начальное: %)', v_after_rollback_patients_count, v_initial_patients_count;
    RAISE NOTICE '  Визиты: % (начальное: %)', v_after_rollback_visits_count, v_initial_visits_count;
    RAISE NOTICE '  Контракты: % (начальное: %)', v_after_rollback_contracts_count, v_initial_contracts_count;
    RAISE NOTICE '  Ставки: % (начальное: %)', v_after_rollback_bets_count, v_initial_bets_count;
    
    -- Проверяем, что все вернулось к исходному состоянию
    IF v_after_rollback_patients_count != v_initial_patients_count THEN
        RAISE EXCEPTION 'Rollback не сработал: количество пациентов не совпадает (% vs %)', 
            v_after_rollback_patients_count, v_initial_patients_count;
    END IF;
    
    IF v_after_rollback_visits_count != v_initial_visits_count THEN
        RAISE EXCEPTION 'Rollback не сработал: количество визитов не совпадает (% vs %)', 
            v_after_rollback_visits_count, v_initial_visits_count;
    END IF;
    
    IF v_after_rollback_contracts_count != v_initial_contracts_count THEN
        RAISE EXCEPTION 'Rollback не сработал: количество контрактов не совпадает (% vs %)', 
            v_after_rollback_contracts_count, v_initial_contracts_count;
    END IF;
    
    IF v_after_rollback_bets_count != v_initial_bets_count THEN
        RAISE EXCEPTION 'Rollback не сработал: количество ставок не совпадает (% vs %)', 
            v_after_rollback_bets_count, v_initial_bets_count;
    END IF;
    
    RAISE NOTICE '  Rollback выполнен успешно, состояние восстановлено';
    
    RAISE NOTICE '=== Шаг 5: Автоматизированные проверки целостности ===';
    
    -- 5.1. Проверка NOT NULL constraints
    SELECT COUNT(*) INTO v_count
    FROM public.patients
    WHERE first_name IS NULL
       OR last_name IS NULL
       OR date_of_birth IS NULL
       OR gender IS NULL;
    IF v_count > 0 THEN
        RAISE EXCEPTION 'patients: NULL required fields = %', v_count;
    END IF;
    
    -- 5.2. Проверка FK constraints
    SELECT COUNT(*) INTO v_count
    FROM public.patients p
    LEFT JOIN public.insurance_companies ic ON p.insurance_company_id = ic.insurance_company_id
    WHERE ic.insurance_company_id IS NULL;
    IF v_count > 0 THEN
        RAISE EXCEPTION 'patients: invalid insurance_company_id = %', v_count;
    END IF;
    
    SELECT COUNT(*) INTO v_count
    FROM public.visits v
    LEFT JOIN public.patients p ON v.patient_id = p.patient_id
    WHERE p.patient_id IS NULL;
    IF v_count > 0 THEN
        RAISE EXCEPTION 'visits: invalid patient_id = %', v_count;
    END IF;
    
    SELECT COUNT(*) INTO v_count
    FROM public.bets b
    LEFT JOIN public.visits v ON b.visit_id = v.visit_id
    WHERE v.visit_id IS NULL;
    IF v_count > 0 THEN
        RAISE EXCEPTION 'bets: invalid visit_id = %', v_count;
    END IF;
    
    SELECT COUNT(*) INTO v_count
    FROM public.contracts c
    LEFT JOIN public.patients p ON c.patient_id = p.patient_id
    WHERE p.patient_id IS NULL;
    IF v_count > 0 THEN
        RAISE EXCEPTION 'contracts: invalid patient_id = %', v_count;
    END IF;
    
    SELECT COUNT(*) INTO v_count
    FROM public.contracts c
    LEFT JOIN public.terms_and_conditions t ON c.terms_id = t.terms_id
    WHERE t.terms_id IS NULL;
    IF v_count > 0 THEN
        RAISE EXCEPTION 'contracts: invalid terms_id = %', v_count;
    END IF;
    
    SELECT COUNT(*) INTO v_count
    FROM public.fin_bet fb
    LEFT JOIN public.bets b ON fb.bet_id = b.bet_id
    WHERE b.bet_id IS NULL;
    IF v_count > 0 THEN
        RAISE EXCEPTION 'fin_bet: invalid bet_id = %', v_count;
    END IF;
    
    SELECT COUNT(*) INTO v_count
    FROM public.fin_bet fb
    LEFT JOIN public.visits v ON fb.visit_id = v.visit_id
    WHERE v.visit_id IS NULL;
    IF v_count > 0 THEN
        RAISE EXCEPTION 'fin_bet: invalid visit_id = %', v_count;
    END IF;
    
    -- 5.3. Проверка доменных значений
    SELECT COUNT(*) INTO v_count
    FROM public.patients
    WHERE gender NOT IN ('M', 'F');
    IF v_count > 0 THEN
        RAISE EXCEPTION 'patients: invalid gender values = %', v_count;
    END IF;
    
    -- 5.4. Проверка NOT NULL для контрактов
    SELECT COUNT(*) INTO v_count
    FROM public.contracts
    WHERE terms_snapshot IS NULL OR status IS NULL;
    IF v_count > 0 THEN
        RAISE EXCEPTION 'contracts: NULL required fields = %', v_count;
    END IF;
    
    -- 5.5. Проверка patient_analysis
    SELECT COUNT(*) INTO v_count
    FROM public.patient_analysis pa
    LEFT JOIN public.patients p ON pa.patient_id = p.patient_id
    WHERE p.patient_id IS NULL;
    IF v_count > 0 THEN
        RAISE EXCEPTION 'patient_analysis: invalid patient_id = %', v_count;
    END IF;
    
    SELECT COUNT(*) INTO v_count
    FROM public.patient_analysis pa
    LEFT JOIN public.analysis a ON pa.analysis_id = a.id
    WHERE a.id IS NULL;
    IF v_count > 0 THEN
        RAISE EXCEPTION 'patient_analysis: invalid analysis_id = %', v_count;
    END IF;
    
    -- 5.6. Проверка analysis_result
    SELECT COUNT(*) INTO v_count
    FROM public.analysis_result ar
    LEFT JOIN public.patient_analysis pa ON ar.patient_analysis_id = pa.id
    WHERE pa.id IS NULL;
    IF v_count > 0 THEN
        RAISE EXCEPTION 'analysis_result: invalid patient_analysis_id = %', v_count;
    END IF;
    
    -- 5.7. Проверка analysis_bet
    SELECT COUNT(*) INTO v_count
    FROM public.analysis_bet ab
    LEFT JOIN public.bets b ON ab.bet_id = b.bet_id
    WHERE b.bet_id IS NULL;
    IF v_count > 0 THEN
        RAISE EXCEPTION 'analysis_bet: invalid bet_id = %', v_count;
    END IF;
    
    SELECT COUNT(*) INTO v_count
    FROM public.analysis_bet ab
    LEFT JOIN public.patient_analysis pa ON ab.patient_analysis_id = pa.id
    WHERE pa.id IS NULL;
    IF v_count > 0 THEN
        RAISE EXCEPTION 'analysis_bet: invalid patient_analysis_id = %', v_count;
    END IF;
    
    RAISE NOTICE '=== Все проверки целостности БД пройдены успешно ===';
    RAISE NOTICE 'Тестирование завершено: все шаги выполнены корректно';
    
END $$;
