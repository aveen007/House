#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9314}"
AUTH_USER="${AUTH_USER:-admin}"
AUTH_PASS="${AUTH_PASS:-password}"

tmp_body="$(mktemp)"
cookie_jar="$(mktemp)"
cleanup() { rm -f "$tmp_body" "$cookie_jar"; }
trap cleanup EXIT

request() {
  local method="$1"
  local url="$2"
  local data="${3:-}"
  local content_type="${4:-application/json}"

  local csrf_token=""
  local csrf_body=""
  if [[ "$method" != "GET" && "$method" != "HEAD" ]]; then
    # Ensure CSRF token is present in cookie jar
    csrf_body="$(curl -sS -u "${AUTH_USER}:${AUTH_PASS}" -b "$cookie_jar" -c "$cookie_jar" "${BASE_URL}/auth/csrf" || true)"
    csrf_token="$(python3 -c 'import json,sys; print(json.load(sys.stdin).get("token",""))' <<<"$csrf_body" 2>/dev/null || true)"
  fi

  if [[ -n "$data" ]]; then
    http_code="$(curl -sS -o "$tmp_body" -w "%{http_code}" \
      -u "${AUTH_USER}:${AUTH_PASS}" \
      -b "$cookie_jar" -c "$cookie_jar" \
      ${csrf_token:+-H "X-XSRF-TOKEN: ${csrf_token}"} \
      -H "Content-Type: ${content_type}" \
      -X "$method" "${BASE_URL}${url}" \
      -d "$data")"
  else
    http_code="$(curl -sS -o "$tmp_body" -w "%{http_code}" \
      -u "${AUTH_USER}:${AUTH_PASS}" \
      -b "$cookie_jar" -c "$cookie_jar" \
      -X "$method" "${BASE_URL}${url}")"
  fi
  echo "$http_code"
}

require_body() {
  if [[ ! -s "$tmp_body" ]]; then
    echo "Empty response body (HTTP ${1:-unknown})."
    exit 1
  fi
}

json_get() {
  python3 -c 'import json,sys
data=json.load(sys.stdin)
path=sys.argv[1].split(".")
cur=data
for part in path:
    if part.endswith("]"):
        name, idx = part[:-1].split("[")
        if name:
            cur = cur[name]
        cur = cur[int(idx)]
    else:
        cur = cur[part]
print(cur)' "$1"
}

echo "API smoke: ${BASE_URL}"

echo "1) Get insurance companies"
code="$(request GET "/api/getInsuranceCompanies")"
if [[ "$code" != "200" ]]; then
  echo "Expected 200, got $code"
  cat "$tmp_body"
  exit 1
fi
require_body "$code"
insurance_id="$(cat "$tmp_body" | json_get "[0].id")"
echo "insurance_id=$insurance_id"

echo "2) Create patient (FR-01)"
suffix="$(date +%s)"
patient_payload="$(cat <<EOF
{"firstName":"Smoke${suffix}","lastName":"Test${suffix}","dateOfBirth":"1990-01-01","gender":"M","insuranceCompanyId":${insurance_id}}
EOF
)"
code="$(request POST "/api/createPatient?verifyInsurance=false" "$patient_payload")"
if [[ "$code" != "201" ]]; then
  echo "Expected 201, got $code"
  cat "$tmp_body"
  exit 1
fi
patient_id="$(cat "$tmp_body" | json_get "id")"
echo "patient_id=$patient_id"

echo "3) Get patients"
code="$(request GET "/api/getPatients")"
[[ "$code" == "200" ]] || { echo "Expected 200, got $code"; cat "$tmp_body"; exit 1; }

echo "4) Get patient by id"
code="$(request GET "/api/getPatient?patientId=${patient_id}")"
[[ "$code" == "200" ]] || { echo "Expected 200, got $code"; cat "$tmp_body"; exit 1; }

echo "5) Create visit (FR-02)"
visit_payload="$(cat <<EOF
{"patientId":${patient_id},"dateOfVisit":"2026-01-19"}
EOF
)"
code="$(request POST "/api/visits" "$visit_payload")"
if [[ "$code" != "201" ]]; then
  echo "Expected 201, got $code"
  cat "$tmp_body"
  exit 1
fi
visit_id="$(cat "$tmp_body" | json_get "id")"
echo "visit_id=$visit_id"

echo "6) Update visit status (FR-04/05)"
code="$(request PUT "/api/visits/${visit_id}/updateHDStatus" "\"Accepted\"" "application/json")"
[[ "$code" == "200" ]] || { echo "Expected 200, got $code"; cat "$tmp_body"; exit 1; }

echo "7) Create bet (FR-09)"
bet_payload="$(cat <<EOF
{"visitId":${visit_id},"diagnosis":"Pneumonia","amount":500}
EOF
)"
code="$(request POST "/api/createBet" "$bet_payload")"
[[ "$code" == "200" ]] || { echo "Expected 200, got $code"; cat "$tmp_body"; exit 1; }
bet_id="$(cat "$tmp_body" | json_get "betId")"
echo "bet_id=$bet_id"

echo "8) Get analyses types"
code="$(request GET "/api/getAnalysesTypes")"
[[ "$code" == "200" ]] || { echo "Expected 200, got $code"; cat "$tmp_body"; exit 1; }
analysis_id="$(cat "$tmp_body" | json_get "[0].id")"
echo "analysis_id=$analysis_id"

echo "9) Create patient analysis (FR-13..16)"
analysis_payload="$(cat <<EOF
{"patientId":${patient_id},"analysisId":${analysis_id},"betId":${bet_id},"date":"2026-01-19","status":"AwaitingHD"}
EOF
)"
code="$(request POST "/api/createPatientAnalysis?verifyInsurance=false" "$analysis_payload")"
[[ "$code" == "201" ]] || { echo "Expected 201, got $code"; cat "$tmp_body"; exit 1; }
patient_analysis_id="$(cat "$tmp_body" | json_get "id")"
echo "patient_analysis_id=$patient_analysis_id"

echo "10) Update patient analysis status (FR-15)"
code="$(request PUT "/api/updatePatientAnalysisStatus?patientAnalysisId=${patient_analysis_id}" "\"Accepted\"" "application/json")"
[[ "$code" == "201" ]] || { echo "Expected 201, got $code"; cat "$tmp_body"; exit 1; }

echo "11) Contracts - get terms"
code="$(request GET "/api/contracts/terms")"
[[ "$code" == "200" ]] || { echo "Expected 200, got $code"; cat "$tmp_body"; exit 1; }
terms_id="$(cat "$tmp_body" | json_get "[0].termsId")"
echo "terms_id=$terms_id"

echo "12) Create contract (FR-06/07)"
contract_payload="$(cat <<EOF
{"patientId":${patient_id},"termsId":${terms_id}}
EOF
)"
code="$(request POST "/api/contracts" "$contract_payload")"
[[ "$code" == "200" || "$code" == "201" ]] || { echo "Expected 200/201, got $code"; cat "$tmp_body"; exit 1; }
contract_id="$(cat "$tmp_body" | json_get "contractId")"
echo "contract_id=$contract_id"

echo "13) Save contract -> READY"
save_payload="$(cat <<EOF
{"status":"READY"}
EOF
)"
code="$(request PUT "/api/contracts/${contract_id}" "$save_payload")"
[[ "$code" == "200" ]] || { echo "Expected 200, got $code"; cat "$tmp_body"; exit 1; }

echo "14) Sign contract (FR-08)"
sign_payload="$(cat <<EOF
{"patientId":${patient_id},"signedBy":"Smoke Tester","signature":"signed"}
EOF
)"
code="$(request POST "/api/contracts/${contract_id}/sign" "$sign_payload")"
[[ "$code" == "200" ]] || { echo "Expected 200, got $code"; cat "$tmp_body"; exit 1; }

echo "15) Get patient contracts"
code="$(request GET "/api/contracts/patient/${patient_id}")"
[[ "$code" == "200" ]] || { echo "Expected 200, got $code"; cat "$tmp_body"; exit 1; }

echo "API smoke completed successfully."

