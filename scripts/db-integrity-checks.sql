\set ON_ERROR_STOP on

DO $$
DECLARE
    v_count integer;
BEGIN
    -- Patients: NOT NULL + FK to insurance_companies
    SELECT COUNT(*) INTO v_count
    FROM public.patients
    WHERE first_name IS NULL
       OR last_name IS NULL
       OR date_of_birth IS NULL
       OR gender IS NULL;
    IF v_count > 0 THEN
        RAISE EXCEPTION 'patients: NULL required fields = %', v_count;
    END IF;

    SELECT COUNT(*) INTO v_count
    FROM public.patients p
    LEFT JOIN public.insurance_companies ic
      ON p.insurance_company_id = ic.insurance_company_id
    WHERE ic.insurance_company_id IS NULL;
    IF v_count > 0 THEN
        RAISE EXCEPTION 'patients: invalid insurance_company_id = %', v_count;
    END IF;

    SELECT COUNT(*) INTO v_count
    FROM public.patients
    WHERE gender NOT IN ('M', 'F');
    IF v_count > 0 THEN
        RAISE EXCEPTION 'patients: invalid gender values = %', v_count;
    END IF;

    -- Visits: FK to patients
    SELECT COUNT(*) INTO v_count
    FROM public.visits v
    LEFT JOIN public.patients p
      ON v.patient_id = p.patient_id
    WHERE p.patient_id IS NULL;
    IF v_count > 0 THEN
        RAISE EXCEPTION 'visits: invalid patient_id = %', v_count;
    END IF;

    -- Bets: FK to visits
    SELECT COUNT(*) INTO v_count
    FROM public.bets b
    LEFT JOIN public.visits v
      ON b.visit_id = v.visit_id
    WHERE v.visit_id IS NULL;
    IF v_count > 0 THEN
        RAISE EXCEPTION 'bets: invalid visit_id = %', v_count;
    END IF;

    -- Patient analysis: FK to patients + analysis
    SELECT COUNT(*) INTO v_count
    FROM public.patient_analysis pa
    LEFT JOIN public.patients p
      ON pa.patient_id = p.patient_id
    WHERE p.patient_id IS NULL;
    IF v_count > 0 THEN
        RAISE EXCEPTION 'patient_analysis: invalid patient_id = %', v_count;
    END IF;

    SELECT COUNT(*) INTO v_count
    FROM public.patient_analysis pa
    LEFT JOIN public.analysis a
      ON pa.analysis_id = a.id
    WHERE a.id IS NULL;
    IF v_count > 0 THEN
        RAISE EXCEPTION 'patient_analysis: invalid analysis_id = %', v_count;
    END IF;

    -- Analysis result: FK to patient_analysis
    SELECT COUNT(*) INTO v_count
    FROM public.analysis_result ar
    LEFT JOIN public.patient_analysis pa
      ON ar.patient_analysis_id = pa.id
    WHERE pa.id IS NULL;
    IF v_count > 0 THEN
        RAISE EXCEPTION 'analysis_result: invalid patient_analysis_id = %', v_count;
    END IF;

    -- Analysis bet: FKs to bets and patient_analysis
    SELECT COUNT(*) INTO v_count
    FROM public.analysis_bet ab
    LEFT JOIN public.bets b
      ON ab.bet_id = b.bet_id
    WHERE b.bet_id IS NULL;
    IF v_count > 0 THEN
        RAISE EXCEPTION 'analysis_bet: invalid bet_id = %', v_count;
    END IF;

    SELECT COUNT(*) INTO v_count
    FROM public.analysis_bet ab
    LEFT JOIN public.patient_analysis pa
      ON ab.patient_analysis_id = pa.id
    WHERE pa.id IS NULL;
    IF v_count > 0 THEN
        RAISE EXCEPTION 'analysis_bet: invalid patient_analysis_id = %', v_count;
    END IF;

    -- Contracts: FK to patients + terms, NOT NULL snapshot/status
    SELECT COUNT(*) INTO v_count
    FROM public.contracts c
    LEFT JOIN public.patients p
      ON c.patient_id = p.patient_id
    WHERE p.patient_id IS NULL;
    IF v_count > 0 THEN
        RAISE EXCEPTION 'contracts: invalid patient_id = %', v_count;
    END IF;

    SELECT COUNT(*) INTO v_count
    FROM public.contracts c
    LEFT JOIN public.terms_and_conditions t
      ON c.terms_id = t.terms_id
    WHERE t.terms_id IS NULL;
    IF v_count > 0 THEN
        RAISE EXCEPTION 'contracts: invalid terms_id = %', v_count;
    END IF;

    SELECT COUNT(*) INTO v_count
    FROM public.contracts
    WHERE terms_snapshot IS NULL
       OR status IS NULL;
    IF v_count > 0 THEN
        RAISE EXCEPTION 'contracts: NULL required fields = %', v_count;
    END IF;

    RAISE NOTICE 'DB integrity checks passed.';
END $$;

