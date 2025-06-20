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

SET default_tablespace = '';

SET default_table_access_method = heap;

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
    date_of_visit date NOT NULL
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
-- Name: bets bet_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.bets ALTER COLUMN bet_id SET DEFAULT nextval('public.bets_bet_id_seq'::regclass);


--
-- Name: insurance_companies insurance_company_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.insurance_companies ALTER COLUMN insurance_company_id SET DEFAULT nextval('public.insurance_companies_insurance_company_id_seq'::regclass);


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
-- Name: fin_bet bet_id_ref; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fin_bet
    ADD CONSTRAINT bet_id_ref FOREIGN KEY (bet_id) REFERENCES public.bets(bet_id) NOT VALID;


--
-- Name: patients insurance_company_ref; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.patients
    ADD CONSTRAINT insurance_company_ref FOREIGN KEY (insurance_company_id) REFERENCES public.insurance_companies(insurance_company_id) ON UPDATE CASCADE ON DELETE CASCADE NOT VALID;


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

