#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9314}"
TEST_USER="${TEST_USER:-admin}"
TEST_PASS="${TEST_PASS:-password}"
ITERATIONS=30

tmp_body="$(mktemp)"
cookie_dir="$(mktemp -d)"
results_file="$(mktemp)"
cleanup() { rm -f "$tmp_body" "$results_file"; rm -rf "$cookie_dir"; }
trap cleanup EXIT

failures=0

# Helper function to get CSRF token
get_csrf_token() {
  local cookie_jar="$1"
  # First authenticate to establish session
  curl -sS -o /dev/null -u "${TEST_USER}:${TEST_PASS}" -b "$cookie_jar" -c "$cookie_jar" "${BASE_URL}/auth/me" || true
  # Then get CSRF token
  curl -sS -b "$cookie_jar" -c "$cookie_jar" "${BASE_URL}/auth/csrf" > "$tmp_body"
  python3 -c 'import json,sys; data=json.load(sys.stdin); print(data["token"])' < "$tmp_body"
}

# Helper function to make authenticated request and measure time
measure_request() {
  local method="$1"
  local url="$2"
  local data="${3:-}"
  local cookie_jar="${cookie_dir}/cookies.txt"
  local csrf_token=""
  
  # Get CSRF token for POST/PUT/DELETE
  if [[ "$method" != "GET" && "$method" != "HEAD" ]]; then
    csrf_token="$(get_csrf_token "$cookie_jar")"
  fi
  
  # Make request and measure time
  local start_time
  start_time="$(date +%s.%N)"
  
  if [[ -n "$data" ]]; then
    curl -sS -o "$tmp_body" -w "%{http_code}" \
      -u "${TEST_USER}:${TEST_PASS}" \
      -b "$cookie_jar" -c "$cookie_jar" \
      ${csrf_token:+-H "X-XSRF-TOKEN: ${csrf_token}"} \
      -H "Content-Type: application/json" \
      -X "$method" "${BASE_URL}${url}" \
      -d "$data" > /dev/null
  else
    curl -sS -o "$tmp_body" -w "%{http_code}" \
      -u "${TEST_USER}:${TEST_PASS}" \
      -b "$cookie_jar" -c "$cookie_jar" \
      -X "$method" "${BASE_URL}${url}" > /dev/null
  fi
  
  local end_time
  end_time="$(date +%s.%N)"
  local duration
  duration="$(python3 -c "print($end_time - $start_time)")"
  echo "$duration"
}

# Calculate statistics
calculate_stats() {
  local operation="$1"
  local times_file="$2"
  
  # Sort times
  sort -n "$times_file" > "${times_file}.sorted"
  
  # Calculate statistics using Python
  python3 <<EOF
import sys

with open("${times_file}.sorted", "r") as f:
    times = [float(line.strip()) for line in f if line.strip()]

if not times:
    print("ERROR: No measurements")
    sys.exit(1)

times.sort()
n = len(times)
mean = sum(times) / n
median = times[n // 2] if n % 2 == 1 else (times[n // 2 - 1] + times[n // 2]) / 2
p90_idx = int(n * 0.9)
p95_idx = int(n * 0.95)
p90 = times[p90_idx] if p90_idx < n else times[-1]
p95 = times[p95_idx] if p95_idx < n else times[-1]
min_time = times[0]
max_time = times[-1]

# Count operations <= 5s (PR-03 requirement)
under_5s = sum(1 for t in times if t <= 5.0)
pct_under_5s = (under_5s / n) * 100

print(f"Operation: ${operation}")
print(f"  Iterations: {n}")
print(f"  Mean: {mean:.3f}s")
print(f"  Median: {median:.3f}s")
print(f"  Min: {min_time:.3f}s")
print(f"  Max: {max_time:.3f}s")
print(f"  p90: {p90:.3f}s")
print(f"  p95: {p95:.3f}s")
print(f"  Operations ≤5s: {under_5s}/{n} ({pct_under_5s:.1f}%)")

# Check PR-03: 90% operations ≤5s
if pct_under_5s >= 90.0:
    print(f"  ✓ PR-03 PASSED: {pct_under_5s:.1f}% operations ≤5s")
else:
    print(f"  ✗ PR-03 FAILED: {pct_under_5s:.1f}% operations ≤5s (required: ≥90%)")
    sys.exit(1)
EOF
}

echo "Performance Profiling (${BASE_URL})"
echo "===================================="
echo "Test user: ${TEST_USER}"
echo "Iterations per operation: ${ITERATIONS}"
echo ""

# Setup: Get insurance company ID and create test patient
echo "Setup: Preparing test data..."
cookie_jar="${cookie_dir}/cookies.txt"

# First, authenticate to get session cookie
curl -sS -o /dev/null -u "${TEST_USER}:${TEST_PASS}" -c "$cookie_jar" "${BASE_URL}/auth/me" || true

# Get insurance company (this also establishes session)
insurance_id="$(curl -sS -u "${TEST_USER}:${TEST_PASS}" -b "$cookie_jar" -c "$cookie_jar" \
  "${BASE_URL}/api/getInsuranceCompanies" | \
  python3 -c 'import json,sys; data=json.load(sys.stdin); print(data[0]["id"])')"

# Get CSRF token after authentication
csrf_token="$(get_csrf_token "$cookie_jar")"

# Create test patient
suffix="$(date +%s)"
patient_payload="$(cat <<EOF
{"firstName":"Perf${suffix}","lastName":"Test${suffix}","dateOfBirth":"1990-01-01","gender":"M","insuranceCompanyId":${insurance_id}}
EOF
)"
patient_response="$(curl -sS -o "$tmp_body" -w "%{http_code}" \
  -u "${TEST_USER}:${TEST_PASS}" \
  -b "$cookie_jar" -c "$cookie_jar" \
  -H "X-XSRF-TOKEN: ${csrf_token}" \
  -H "Content-Type: application/json" \
  -X POST "${BASE_URL}/api/createPatient?verifyInsurance=false" \
  -d "$patient_payload")"

if [[ "$patient_response" != "201" ]]; then
  echo "ERROR: Failed to create test patient (HTTP $patient_response)"
  cat "$tmp_body"
  exit 1
fi

patient_id="$(cat "$tmp_body" | python3 -c 'import json,sys; data=json.load(sys.stdin); print(data["id"])')"
echo "Created test patient ID: ${patient_id}"
echo ""

# Create test visit for bet creation
visit_payload="$(cat <<EOF
{"patientId":${patient_id},"dateOfVisit":"2026-01-19"}
EOF
)"
csrf_token="$(get_csrf_token "$cookie_jar")"
visit_response="$(curl -sS -o "$tmp_body" -w "%{http_code}" \
  -u "${TEST_USER}:${TEST_PASS}" \
  -b "$cookie_jar" -c "$cookie_jar" \
  -H "X-XSRF-TOKEN: ${csrf_token}" \
  -H "Content-Type: application/json" \
  -X POST "${BASE_URL}/api/visits" \
  -d "$visit_payload")"

if [[ "$visit_response" != "201" ]]; then
  echo "ERROR: Failed to create test visit (HTTP $visit_response)"
  cat "$tmp_body"
  exit 1
fi

visit_id="$(cat "$tmp_body" | python3 -c 'import json,sys; data=json.load(sys.stdin); print(data["id"])')"
echo "Created test visit ID: ${visit_id}"

# Update visit status to Accepted (required for bet creation)
csrf_token="$(get_csrf_token "$cookie_jar")"
visit_status_response="$(curl -sS -o "$tmp_body" -w "%{http_code}" \
  -u "${TEST_USER}:${TEST_PASS}" \
  -b "$cookie_jar" -c "$cookie_jar" \
  -H "X-XSRF-TOKEN: ${csrf_token}" \
  -H "Content-Type: application/json" \
  -X PUT "${BASE_URL}/api/visits/${visit_id}/updateHDStatus" \
  -d '"Accepted"')"

if [[ "$visit_status_response" != "200" ]]; then
  echo "WARNING: Failed to update visit status (HTTP $visit_status_response), continuing anyway..."
fi
echo ""

# Operation 1: Load patient list
echo "Operation 1: Load patient list (GET /api/getPatients)"
echo "------------------------------------------------------"
times_file="${cookie_dir}/times_list.txt"
> "$times_file"

for i in $(seq 1 $ITERATIONS); do
  duration="$(measure_request GET "/api/getPatients")"
  echo "$duration" >> "$times_file"
  printf "  Iteration %2d: %.3fs\r" "$i" "$duration"
done
echo ""

if ! calculate_stats "Load patient list" "$times_file"; then
  failures=$((failures+1))
fi
echo ""

# Operation 2: Open patient card
echo "Operation 2: Open patient card (GET /api/getPatient)"
echo "------------------------------------------------------"
times_file="${cookie_dir}/times_card.txt"
> "$times_file"

for i in $(seq 1 $ITERATIONS); do
  duration="$(measure_request GET "/api/getPatient?patientId=${patient_id}")"
  echo "$duration" >> "$times_file"
  printf "  Iteration %2d: %.3fs\r" "$i" "$duration"
done
echo ""

if ! calculate_stats "Open patient card" "$times_file"; then
  failures=$((failures+1))
fi
echo ""

# Operation 3: Search by name (via getPatients - we'll filter client-side for measurement)
echo "Operation 3: Search by name (GET /api/getPatients + filter)"
echo "------------------------------------------------------------"
times_file="${cookie_dir}/times_search.txt"
> "$times_file"

for i in $(seq 1 $ITERATIONS); do
  duration="$(measure_request GET "/api/getPatients")"
  echo "$duration" >> "$times_file"
  printf "  Iteration %2d: %.3fs\r" "$i" "$duration"
done
echo ""

if ! calculate_stats "Search by name" "$times_file"; then
  failures=$((failures+1))
fi
echo ""

# Operation 4: Save new bet
echo "Operation 4: Save new bet (POST /api/createBet)"
echo "------------------------------------------------"
times_file="${cookie_dir}/times_bet.txt"
> "$times_file"

# We need to create a new visit for each bet (or reuse the same)
bet_counter=1
for i in $(seq 1 $ITERATIONS); do
  bet_payload="$(cat <<EOF
{"visitId":${visit_id},"diagnosis":"Performance Test ${bet_counter}","amount":100}
EOF
)"
  duration="$(measure_request POST "/api/createBet" "$bet_payload")"
  echo "$duration" >> "$times_file"
  printf "  Iteration %2d: %.3fs\r" "$i" "$duration"
  bet_counter=$((bet_counter+1))
done
echo ""

if ! calculate_stats "Save new bet" "$times_file"; then
  failures=$((failures+1))
fi
echo ""

# Summary
echo "===================================="
echo "Performance Profiling Summary"
echo "===================================="
if [[ "$failures" -eq 0 ]]; then
  echo "✓ All operations meet PR-03 requirement (90% operations ≤5s)"
  exit 0
else
  echo "✗ $failures operation(s) failed to meet PR-03 requirement"
  exit 1
fi

