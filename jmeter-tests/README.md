# JMeter Performance Testing

This directory contains JMeter test plans for Load Testing and Stress Testing of the Medical Application API.

## Directory Structure

```
jmeter-tests/
├── load-test/
│   └── load_test_plan.jmx          # Load Testing test plan (50-100 users)
├── stress-test/
│   └── stress_test_plan.jmx        # Stress Testing test plan (step-wise: 50-200 users)
├── results/                         # Test execution results
└── README.md                        # This file
```

## Prerequisites

1. **JMeter 5.6.3+** installed (verified: `jmeter --version`)
2. **Application running** on `http://localhost:8080`
3. **Database** with initial data (insurance companies, symptoms, etc.)

## Test Plans Overview

### Load Testing (load_test_plan.jmx)

**Objective**: Test system behavior under declared maximum concurrent load (50 users target, 100 users peak)

**User Distribution**:
- 20 users: View/filter patient list (GET /api/getPatients)
- 15 users: Create/edit patient records (POST /api/createPatient, PUT /api/updatePatient)
- 10 users: Work with bets (GET /api/getBetPatients, POST /api/createBet)
- 5 users: Call external APIs (POST /api/insurance/check)

**Load Profile**:
- **Target Load (50 users)**: Ramp-up 0→50 over 5 minutes, hold 30 minutes, then ramp-down
- **Peak Load (100 users)**: Ramp-up 0→100 over 10 minutes, hold 30 minutes, then ramp-down

**Success Criteria**:
- At 50 users: 90% of operations ≤5s
- At 100 users: 90% of operations ≤8s
- HTTP 5xx errors ≤1%
- CPU < 85%, Memory < 90%

### Stress Testing (stress_test_plan.jmx)

**Objective**: Determine system limits, breaking point, and recovery characteristics

**Load Steps**: 50, 75, 100, 125, 150, 200 users
- Each step: Ramp-up over 3 minutes, hold 10 minutes
- Monitor: latency (p50/p90/p99), error rates (4xx/5xx), CPU, memory, DB connections
- Stop when 5xx errors > 5% or latency p99 > 30s

**Success Criteria**:
- Identify degradation point
- System recovers to normal state ≤10 minutes after load removal

## Running Tests

### Using JMeter GUI

1. **Open JMeter GUI**:
   ```bash
   jmeter
   ```

2. **Load Test Plan**:
   - File → Open → Select `load_test_plan.jmx` or `stress_test_plan.jmx`

3. **Configure Server** (if needed):
   - Update `server.hostname` variable if not using localhost:8080

4. **Run Test**:
   - Click "Run" → "Start" (Ctrl+R)
   - Monitor results in "View Results Tree" and "Summary Report"

5. **Save Results**:
   - File → "Save Test Plan As..." (to save any modifications)

### Using JMeter CLI (Non-GUI Mode)

**Load Testing** (50 users):
```bash
cd /Users/daniel/Documents/programming/mpi_proj/House
jmeter -n -t jmeter-tests/load-test/load_test_plan.jmx \
  -l jmeter-tests/results/load_test_50_users_$(date +%Y%m%d_%H%M%S).jtl \
  -e -o jmeter-tests/results/load_test_50_users_report_$(date +%Y%m%d_%H%M%S)/
```

**Load Testing** (100 users - peak):
```bash
# Update Thread Group "Users" to 100 in JMX file first, or use parameter
jmeter -n -t jmeter-tests/load-test/load_test_plan.jmx \
  -Jusers=100 \
  -l jmeter-tests/results/load_test_100_users_$(date +%Y%m%d_%H%M%S).jtl \
  -e -o jmeter-tests/results/load_test_100_users_report_$(date +%Y%m%d_%H%M%S)/
```

**Stress Testing**:
```bash
jmeter -n -t jmeter-tests/stress-test/stress_test_plan.jmx \
  -l jmeter-tests/results/stress_test_$(date +%Y%m%d_%H%M%S).jtl \
  -e -o jmeter-tests/results/stress_test_report_$(date +%Y%m%d_%H%M%S)/
```

**Parameters**:
- `-n`: Non-GUI mode
- `-t`: Test plan file
- `-l`: Results log file (.jtl)
- `-e`: Generate HTML report after test
- `-o`: HTML report output directory
- `-Jusers=100`: Set JMeter property (if supported by test plan)

## Test Plan Structure

### Thread Groups

Each test plan contains multiple Thread Groups representing different user types:

1. **View Patients Thread Group** (40% of users)
   - GET /api/getPatients
   - GET /api/getPatient?patientId={id}

2. **Manage Patients Thread Group** (30% of users)
   - POST /api/createPatient
   - PUT /api/updatePatient
   - GET /api/getPatients

3. **Bets Thread Group** (20% of users)
   - GET /api/getBetPatients
   - POST /api/createBet
   - GET /api/getVisitBets?visitId={id}

4. **External APIs Thread Group** (10% of users)
   - POST /api/insurance/check

### Listeners

- **View Results Tree**: Detailed request/response view (use in GUI only, disable in CLI)
- **Summary Report**: Aggregate statistics
- **Response Times Over Time**: Response time graphs
- **Aggregate Report**: Statistical summary
- **HTML Report**: Generated after CLI execution

## API Endpoints Reference

### Base URL
```
http://localhost:8080
```

### Patient Endpoints
- `GET /api/getPatients` - Get all patients
- `GET /api/getPatient?patientId={id}` - Get patient by ID
- `POST /api/createPatient` - Create patient
  ```json
  {
    "firstName": "John",
    "lastName": "Doe",
    "dateOfBirth": "1990-01-01",
    "gender": "M",
    "insuranceCompanyId": 1
  }
  ```
- `PUT /api/updatePatient` - Update patient
- `DELETE /api/deletePatient?patientId={id}` - Delete patient

### Visit Endpoints
- `POST /api/visits` - Create visit
  ```json
  {
    "patientId": 1,
    "dateOfVisit": "2024-01-15"
  }
  ```
- `GET /api/visits/getAllAcceptedVisits` - Get accepted visits
- `GET /api/visits/getAllHDAwaitingVisits` - Get HD awaiting visits
- `GET /api/visits/getAllPatientVisits?patientId={id}` - Get patient visits

### Bet Endpoints
- `GET /api/getBetPatients` - Get bet patients
- `GET /api/getVisitBets?visitId={id}` - Get visit bets
- `POST /api/createBet` - Create bet

### Insurance Endpoints
- `POST /api/insurance/check` - Check insurance (external API simulation)
- `GET /api/insuranceCompanies` - Get insurance companies

### Reference Data
- Insurance Company ID: `1` (TestInsurance)
- Symptom IDs: `1-5` (Fever, Cough, Fatigue, Headache, Diarrhea)

## Monitoring

While tests are running, monitor:

1. **Application Metrics**:
   - CPU usage
   - Memory usage
   - Database connections
   - Request latency

2. **JMeter Metrics**:
   - Response times (min/max/avg/p90/p99)
   - Throughput (requests/second)
   - Error rate (%)
   - Active threads

3. **Database Metrics**:
   - Query execution time
   - Connection pool usage
   - Lock contention

## Troubleshooting

### Common Issues

1. **"Connection refused" errors**:
   - Ensure application is running on port 8080
   - Check firewall settings

2. **High error rates**:
   - Check application logs
   - Verify database is accessible
   - Check available database connections

3. **Out of memory errors in JMeter**:
   - Increase JMeter heap size: `JVM_ARGS="-Xms1g -Xmx4g" jmeter`
   - Reduce number of threads or test duration

4. **Tests run too slowly**:
   - Disable "View Results Tree" listener in CLI mode
   - Reduce test duration or number of threads
   - Check system resources (CPU, memory)

## Results Interpretation

### Key Metrics

- **Response Time**: Time to receive response (lower is better)
- **Throughput**: Requests per second (higher is better)
- **Error Rate**: Percentage of failed requests (lower is better)
- **Latency Percentiles**: p50 (median), p90, p99 (worst cases)

### HTML Report Sections

- **Dashboard**: Overview with key metrics
- **Statistics**: Detailed statistics per request
- **Over Time**: Graphs showing metrics over time
- **Throughput**: Request throughput analysis
- **Response Times**: Response time distribution

## Notes

- Always run tests against a test database, not production
- Consider running tests during off-peak hours
- Save test results for comparison
- Document any configuration changes
- Review application logs after test execution

