--
-- PostgreSQL database dump
--

-- Dumped from database version 16.2
-- Dumped by pg_dump version 16.2

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: AnalysisStatus; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public."AnalysisStatus" AS ENUM (
    'AwaitingHD',
    'AwaitingPat',
    'Rejected',
    'Accepted',
    'Finished'
);


ALTER TYPE public."AnalysisStatus" OWNER TO postgres;

--
-- Name: VisitHDStatus; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public."VisitHDStatus" AS ENUM (
    'Awaiting',
    'Accepted',
    'Rejected'
);


ALTER TYPE public."VisitHDStatus" OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: analysis; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.analysis (
    id integer NOT NULL,
    title character varying NOT NULL
);


ALTER TABLE public.analysis OWNER TO postgres;

--
-- Name: analysis_bet; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.analysis_bet (
    bet_id integer NOT NULL,
    patient_analysis_id integer NOT NULL
);


ALTER TABLE public.analysis_bet OWNER TO postgres;

--
-- Name: analysis_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.analysis_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.analysis_id_seq OWNER TO postgres;

--
-- Name: analysis_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.analysis_id_seq OWNED BY public.analysis.id;


--
-- Name: analysis_result; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.analysis_result (
    id integer NOT NULL,
    patient_analysis_id integer NOT NULL,
    result character varying NOT NULL
);


ALTER TABLE public.analysis_result OWNER TO postgres;

--
-- Name: analysis_result_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.analysis_result_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.analysis_result_id_seq OWNER TO postgres;

--
-- Name: analysis_result_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.analysis_result_id_seq OWNED BY public.analysis_result.id;


--
-- Name: bets; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.bets (
    bet_id integer NOT NULL,
    visit_id integer NOT NULL,
    diagnosis character varying NOT NULL,
    amount bigint NOT NULL
);


ALTER TABLE public.bets OWNER TO postgres;

--
-- Name: bets_bet_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.bets_bet_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.bets_bet_id_seq OWNER TO postgres;

--
-- Name: bets_bet_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.bets_bet_id_seq OWNED BY public.bets.bet_id;


--
-- Name: fin_bet; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.fin_bet (
    bet_id integer NOT NULL,
    visit_id integer NOT NULL
);


ALTER TABLE public.fin_bet OWNER TO postgres;

--
-- Name: insurance_companies; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.insurance_companies (
    insurance_company_id integer NOT NULL,
    company_name character varying NOT NULL,
    api_url character varying NOT NULL,
    key character varying NOT NULL
);


ALTER TABLE public.insurance_companies OWNER TO postgres;

--
-- Name: insurance_companies_insurance_company_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.insurance_companies_insurance_company_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.insurance_companies_insurance_company_id_seq OWNER TO postgres;

--
-- Name: insurance_companies_insurance_company_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.insurance_companies_insurance_company_id_seq OWNED BY public.insurance_companies.insurance_company_id;


--
-- Name: patient_analysis; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.patient_analysis (
    id integer NOT NULL,
    patient_id integer NOT NULL,
    analysis_id integer NOT NULL,
    date date,
    status integer
);


ALTER TABLE public.patient_analysis OWNER TO postgres;

--
-- Name: patient_analysis_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.patient_analysis_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.patient_analysis_id_seq OWNER TO postgres;

--
-- Name: patient_analysis_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.patient_analysis_id_seq OWNED BY public.patient_analysis.id;


--
-- Name: patient_insurance; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.patient_insurance (
    id integer NOT NULL,
    patient_first_name character varying NOT NULL,
    patient_last_name character varying NOT NULL,
    company_name character varying NOT NULL
);


ALTER TABLE public.patient_insurance OWNER TO postgres;

--
-- Name: patient_insurance_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.patient_insurance_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.patient_insurance_id_seq OWNER TO postgres;

--
-- Name: patient_insurance_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.patient_insurance_id_seq OWNED BY public.patient_insurance.id;


--
-- Name: patients; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.patients (
    patient_id integer NOT NULL,
    first_name character varying NOT NULL,
    last_name character varying NOT NULL,
    date_of_birth date NOT NULL,
    gender character varying(1) NOT NULL,
    insurance_company_id integer NOT NULL
);


ALTER TABLE public.patients OWNER TO postgres;

--
-- Name: patients_patient_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.patients_patient_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.patients_patient_id_seq OWNER TO postgres;

--
-- Name: patients_patient_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.patients_patient_id_seq OWNED BY public.patients.patient_id;


--
-- Name: symptom; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.symptom (
    symptom_id integer NOT NULL,
    symptom_name character varying NOT NULL
);


ALTER TABLE public.symptom OWNER TO postgres;

--
-- Name: symptom_symptom_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.symptom_symptom_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.symptom_symptom_id_seq OWNER TO postgres;

--
-- Name: symptom_symptom_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.symptom_symptom_id_seq OWNED BY public.symptom.symptom_id;


--
-- Name: visit_symptom; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.visit_symptom (
    visit_id integer NOT NULL,
    symptom_id integer NOT NULL
);


ALTER TABLE public.visit_symptom OWNER TO postgres;

--
-- Name: visits; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.visits (
    visit_id integer NOT NULL,
    patient_id integer NOT NULL,
    date_of_visit date NOT NULL,
    hd_status integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.visits OWNER TO postgres;

--
-- Name: visits_visit_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.visits_visit_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.visits_visit_id_seq OWNER TO postgres;

--
-- Name: visits_visit_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.visits_visit_id_seq OWNED BY public.visits.visit_id;


--
-- Name: analysis id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.analysis ALTER COLUMN id SET DEFAULT nextval('public.analysis_id_seq'::regclass);


--
-- Name: analysis_result id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.analysis_result ALTER COLUMN id SET DEFAULT nextval('public.analysis_result_id_seq'::regclass);


--
-- Name: bets bet_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.bets ALTER COLUMN bet_id SET DEFAULT nextval('public.bets_bet_id_seq'::regclass);


--
-- Name: insurance_companies insurance_company_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.insurance_companies ALTER COLUMN insurance_company_id SET DEFAULT nextval('public.insurance_companies_insurance_company_id_seq'::regclass);


--
-- Name: patient_analysis id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.patient_analysis ALTER COLUMN id SET DEFAULT nextval('public.patient_analysis_id_seq'::regclass);


--
-- Name: patient_insurance id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.patient_insurance ALTER COLUMN id SET DEFAULT nextval('public.patient_insurance_id_seq'::regclass);


--
-- Name: patients patient_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.patients ALTER COLUMN patient_id SET DEFAULT nextval('public.patients_patient_id_seq'::regclass);


--
-- Name: symptom symptom_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.symptom ALTER COLUMN symptom_id SET DEFAULT nextval('public.symptom_symptom_id_seq'::regclass);


--
-- Name: visits visit_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.visits ALTER COLUMN visit_id SET DEFAULT nextval('public.visits_visit_id_seq'::regclass);


--
-- Data for Name: analysis; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.analysis (id, title) FROM stdin;
1	Blood Chemistry Analysis
2	Genetic Testing
3	Liver Function Tests
4	Urinalysis
5	Basic Metabolic Panel
\.


--
-- Data for Name: analysis_bet; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.analysis_bet (bet_id, patient_analysis_id) FROM stdin;
1	2
\.


--
-- Data for Name: analysis_result; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.analysis_result (id, patient_analysis_id, result) FROM stdin;
1	1	check result
\.


--
-- Data for Name: bets; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.bets (bet_id, visit_id, diagnosis, amount) FROM stdin;
1	2	Death	500
\.


--
-- Data for Name: fin_bet; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.fin_bet (bet_id, visit_id) FROM stdin;
1	2
\.


--
-- Data for Name: insurance_companies; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.insurance_companies (insurance_company_id, company_name, api_url, key) FROM stdin;
1	TestInsurance	http://localhost:8080/api/insurance_check	nothing
\.


--
-- Data for Name: patient_analysis; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.patient_analysis (id, patient_id, analysis_id, date, status) FROM stdin;
2	6	2	1990-01-01	0
1	6	1	1990-01-01	1
\.


--
-- Data for Name: patient_insurance; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.patient_insurance (id, patient_first_name, patient_last_name, company_name) FROM stdin;
1	John	Doe	TestInsurance
\.


--
-- Data for Name: patients; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.patients (patient_id, first_name, last_name, date_of_birth, gender, insurance_company_id) FROM stdin;
6	John	Doe	1990-01-01	M	1
\.


--
-- Data for Name: symptom; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.symptom (symptom_id, symptom_name) FROM stdin;
1	Fever
2	Cough
3	Fatigue
4	Headache
5	Diarrhea
\.


--
-- Data for Name: visit_symptom; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.visit_symptom (visit_id, symptom_id) FROM stdin;
2	1
2	4
2	3
4	3
4	1
3	2
3	4
\.


--
-- Data for Name: visits; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.visits (visit_id, patient_id, date_of_visit, hd_status) FROM stdin;
2	6	2022-01-01	0
3	6	2021-01-01	0
4	6	2023-01-01	0
5	6	2023-01-02	0
7	6	2023-03-02	1
\.


--
-- Name: analysis_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.analysis_id_seq', 5, true);


--
-- Name: analysis_result_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.analysis_result_id_seq', 1, true);


--
-- Name: bets_bet_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.bets_bet_id_seq', 1, true);


--
-- Name: insurance_companies_insurance_company_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.insurance_companies_insurance_company_id_seq', 1, false);


--
-- Name: patient_analysis_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.patient_analysis_id_seq', 2, true);


--
-- Name: patient_insurance_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.patient_insurance_id_seq', 1, true);


--
-- Name: patients_patient_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.patients_patient_id_seq', 6, true);


--
-- Name: symptom_symptom_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.symptom_symptom_id_seq', 5, true);


--
-- Name: visits_visit_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.visits_visit_id_seq', 7, true);


--
-- Name: analysis_bet analysis_bet_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.analysis_bet
    ADD CONSTRAINT analysis_bet_pkey PRIMARY KEY (bet_id, patient_analysis_id);


--
-- Name: analysis analysis_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.analysis
    ADD CONSTRAINT analysis_pkey PRIMARY KEY (id);


--
-- Name: analysis_result analysis_result_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.analysis_result
    ADD CONSTRAINT analysis_result_pkey PRIMARY KEY (id);


--
-- Name: bets bets_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.bets
    ADD CONSTRAINT bets_pkey PRIMARY KEY (bet_id);


--
-- Name: fin_bet fin_bet_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fin_bet
    ADD CONSTRAINT fin_bet_pkey PRIMARY KEY (bet_id, visit_id);


--
-- Name: insurance_companies insurance_companies_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.insurance_companies
    ADD CONSTRAINT insurance_companies_pkey PRIMARY KEY (insurance_company_id);


--
-- Name: patient_analysis patient_analysis_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.patient_analysis
    ADD CONSTRAINT patient_analysis_pkey PRIMARY KEY (id);


--
-- Name: patient_insurance patient_insurance_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.patient_insurance
    ADD CONSTRAINT patient_insurance_pkey PRIMARY KEY (id);


--
-- Name: patients patients_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.patients
    ADD CONSTRAINT patients_pkey PRIMARY KEY (patient_id);


--
-- Name: symptom symptom_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.symptom
    ADD CONSTRAINT symptom_pkey PRIMARY KEY (symptom_id);


--
-- Name: visit_symptom visit_symptom_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.visit_symptom
    ADD CONSTRAINT visit_symptom_pkey PRIMARY KEY (visit_id, symptom_id);


--
-- Name: visits visits_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.visits
    ADD CONSTRAINT visits_pkey PRIMARY KEY (visit_id);


--
-- Name: patient_analysis analysis check; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.patient_analysis
    ADD CONSTRAINT "analysis check" FOREIGN KEY (analysis_id) REFERENCES public.analysis(id) NOT VALID;


--
-- Name: fin_bet bet_id_ref; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fin_bet
    ADD CONSTRAINT bet_id_ref FOREIGN KEY (bet_id) REFERENCES public.bets(bet_id) NOT VALID;


--
-- Name: analysis_bet check analysis id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.analysis_bet
    ADD CONSTRAINT "check analysis id" FOREIGN KEY (patient_analysis_id) REFERENCES public.patient_analysis(id) NOT VALID;


--
-- Name: analysis_bet check bet id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.analysis_bet
    ADD CONSTRAINT "check bet id" FOREIGN KEY (bet_id) REFERENCES public.bets(bet_id);


--
-- Name: analysis_result id check; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.analysis_result
    ADD CONSTRAINT "id check" FOREIGN KEY (patient_analysis_id) REFERENCES public.patient_analysis(id);


--
-- Name: patients insurance_company_ref; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.patients
    ADD CONSTRAINT insurance_company_ref FOREIGN KEY (insurance_company_id) REFERENCES public.insurance_companies(insurance_company_id) ON UPDATE CASCADE ON DELETE CASCADE NOT VALID;


--
-- Name: patient_analysis patient check; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.patient_analysis
    ADD CONSTRAINT "patient check" FOREIGN KEY (patient_id) REFERENCES public.patients(patient_id) NOT VALID;


--
-- Name: visits patient_ref; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.visits
    ADD CONSTRAINT patient_ref FOREIGN KEY (patient_id) REFERENCES public.patients(patient_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: visit_symptom symptom_ref; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.visit_symptom
    ADD CONSTRAINT symptom_ref FOREIGN KEY (symptom_id) REFERENCES public.symptom(symptom_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: visit_symptom visit_ref; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.visit_symptom
    ADD CONSTRAINT visit_ref FOREIGN KEY (visit_id) REFERENCES public.visits(visit_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: bets visit_ref; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.bets
    ADD CONSTRAINT visit_ref FOREIGN KEY (visit_id) REFERENCES public.visits(visit_id) NOT VALID;


--
-- Name: fin_bet visit_ref; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fin_bet
    ADD CONSTRAINT visit_ref FOREIGN KEY (visit_id) REFERENCES public.visits(visit_id) NOT VALID;


--
-- PostgreSQL database dump complete
--

-- 1) Base Terms & Conditions (версии условий)
CREATE TABLE public.terms_and_conditions (
    terms_id      integer NOT NULL,
    title         character varying NOT NULL,
    content       text NOT NULL,
    version       integer NOT NULL,
    is_active     boolean NOT NULL DEFAULT false,
    created_at    timestamptz NOT NULL DEFAULT now()
);

CREATE SEQUENCE public.terms_and_conditions_terms_id_seq
    AS integer START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;

ALTER SEQUENCE public.terms_and_conditions_terms_id_seq
    OWNED BY public.terms_and_conditions.terms_id;

ALTER TABLE ONLY public.terms_and_conditions
    ALTER COLUMN terms_id SET DEFAULT nextval('public.terms_and_conditions_terms_id_seq'::regclass);

ALTER TABLE ONLY public.terms_and_conditions
    ADD CONSTRAINT terms_and_conditions_pkey PRIMARY KEY (terms_id);

ALTER TABLE ONLY public.terms_and_conditions
    ADD CONSTRAINT terms_and_conditions_version_uq UNIQUE (version);


-- 2) Contracts (контракты пациента)
CREATE TABLE public.contracts (
    contract_id     integer NOT NULL,
    patient_id      integer NOT NULL,
    terms_id        integer NOT NULL,
    
    -- Снимок условий на момент создания контракта
    terms_snapshot  text NOT NULL,

    -- DRAFT / READY / SIGNED / REVOKED 
    status          character varying NOT NULL DEFAULT 'DRAFT',

    created_at      timestamptz NOT NULL DEFAULT now(),
    updated_at      timestamptz NOT NULL DEFAULT now(),
    signed_at       timestamptz,

    signed_by       character varying,
    signature       character varying
);

CREATE SEQUENCE public.contracts_contract_id_seq
    AS integer START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;

ALTER SEQUENCE public.contracts_contract_id_seq
    OWNED BY public.contracts.contract_id;

ALTER TABLE ONLY public.contracts
    ALTER COLUMN contract_id SET DEFAULT nextval('public.contracts_contract_id_seq'::regclass);

ALTER TABLE ONLY public.contracts
    ADD CONSTRAINT contracts_pkey PRIMARY KEY (contract_id);

-- FK на пациента
ALTER TABLE ONLY public.contracts
    ADD CONSTRAINT contracts_patient_fk
    FOREIGN KEY (patient_id)
    REFERENCES public.patients(patient_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT;

-- FK на terms
ALTER TABLE ONLY public.contracts
    ADD CONSTRAINT contracts_terms_fk
    FOREIGN KEY (terms_id)
    REFERENCES public.terms_and_conditions(terms_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT;

-- Индекс для быстрых проверок "подписан ли контракт у пациента"
CREATE INDEX contracts_patient_status_idx
    ON public.contracts (patient_id, status);

-- Вставка тестовых условий (Terms & Conditions)
INSERT INTO public.terms_and_conditions (title, content, version, is_active) VALUES
('Общие условия медицинского обслуживания v1', 
 'Настоящие условия регулируют предоставление медицинских услуг пациентам нашей клиники.
 
1. ОБЩИЕ ПОЛОЖЕНИЯ
Пациент обязуется предоставить достоверную информацию о своем состоянии здоровья.

2. ПРАВА И ОБЯЗАННОСТИ ПАЦИЕНТА
- Пациент имеет право на конфиденциальность медицинской информации
- Пациент обязуется своевременно оплачивать медицинские услуги
- Пациент имеет право на получение качественной медицинской помощи

3. ОБРАБОТКА ПЕРСОНАЛЬНЫХ ДАННЫХ
Клиника обязуется обрабатывать персональные данные в соответствии с законодательством.

4. УСЛОВИЯ ОПЛАТЫ
Оплата производится согласно тарифам, действующим на дату оказания услуг.

5. ОТВЕТСТВЕННОСТЬ СТОРОН
Стороны несут ответственность согласно законодательству РФ.',
 1, true),

('Условия медицинского обслуживания v2 (расширенные)', 
 'Настоящие условия регулируют предоставление медицинских услуг пациентам нашей клиники (обновленная версия).
 
1. ОБЩИЕ ПОЛОЖЕНИЯ
1.1. Пациент обязуется предоставить достоверную информацию о своем состоянии здоровья.
1.2. Пациент должен информировать врача о принимаемых лекарственных препаратах.

2. ПРАВА И ОБЯЗАННОСТИ ПАЦИЕНТА
- Пациент имеет право на конфиденциальность медицинской информации
- Пациент обязуется своевременно оплачивать медицинские услуги
- Пациент имеет право на получение качественной медицинской помощи
- Пациент имеет право на получение копии медицинских документов

3. ОБРАБОТКА ПЕРСОНАЛЬНЫХ ДАННЫХ
3.1. Клиника обязуется обрабатывать персональные данные в соответствии с законодательством.
3.2. Пациент дает согласие на обработку персональных данных, включая медицинскую информацию.

4. УСЛОВИЯ ОПЛАТЫ
4.1. Оплата производится согласно тарифам, действующим на дату оказания услуг.
4.2. При наличии страховки, оплата производится в соответствии с условиями страхования.

5. ОТВЕТСТВЕННОСТЬ СТОРОН
5.1. Стороны несут ответственность согласно законодательству РФ.
5.2. Клиника не несет ответственность за результаты лечения при несоблюдении пациентом рекомендаций врача.

6. ПОРЯДОК РАЗРЕШЕНИЯ СПОРОВ
Споры разрешаются путем переговоров, а при невозможности достижения согласия - в судебном порядке.',
 2, true),

('Условия VIP обслуживания v1', 
 'Настоящие условия регулируют предоставление VIP медицинских услуг.
 
1. УРОВЕНЬ ОБСЛУЖИВАНИЯ
VIP пациенты получают приоритетное обслуживание без очереди.

2. ДОПОЛНИТЕЛЬНЫЕ УСЛУГИ
- Персональный менеджер
- Индивидуальная палата
- Расширенное питание
- Бесплатная парковка

3. УСЛОВИЯ ОПЛАТЫ
Оплата производится по специальному тарифу VIP обслуживания.

4. ПРОЧИЕ УСЛОВИЯ
Применяются все условия стандартного договора с дополнениями согласно VIP пакету.',
 3, false);