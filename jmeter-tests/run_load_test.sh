#!/bin/bash

# Load Testing Script (TestPlan 5.2.6)
# This script runs Load Testing with JMeter in non-GUI mode
# According to TestPlan: 50 users (target) and 100 users (peak)

set -euo pipefail

# Configuration
TEST_PLAN_DIR="$(cd "$(dirname "$0")" && pwd)"
RESULTS_DIR="${TEST_PLAN_DIR}/results"
LOAD_TEST_PLAN="${TEST_PLAN_DIR}/load-test/load_test_plan.jmx"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Create results directory if it doesn't exist
mkdir -p "${RESULTS_DIR}"

# Check if JMeter is installed
if ! command -v jmeter &> /dev/null; then
    echo -e "${RED}Error: JMeter is not installed or not in PATH${NC}"
    echo "Please install JMeter: brew install jmeter"
    exit 1
fi

# Check if test plan exists
if [ ! -f "${LOAD_TEST_PLAN}" ]; then
    echo -e "${RED}Error: Test plan not found at ${LOAD_TEST_PLAN}${NC}"
    exit 1
fi

# Parse arguments
QUICK_TEST=false
USERS=50

while [[ $# -gt 0 ]]; do
    case $1 in
        --quick|-q)
            QUICK_TEST=true
            shift
            ;;
        *)
            if [[ "$1" =~ ^[0-9]+$ ]]; then
                USERS=$1
            fi
            shift
            ;;
    esac
done

TIMESTAMP=$(date +%Y%m%d_%H%M%S)
JTL_FILE="${RESULTS_DIR}/load_test_${USERS}_users_${TIMESTAMP}.jtl"
HTML_REPORT_DIR="${RESULTS_DIR}/load_test_${USERS}_users_report_${TIMESTAMP}"
CSV_FILE="${RESULTS_DIR}/load_test_${USERS}_users_${TIMESTAMP}.csv"

# Function to extract metrics from JTL file
extract_metrics() {
    local jtl_file=$1
    
    if [ ! -f "${jtl_file}" ]; then
        echo "0,0,0,0,0,0,0,0"
        return
    fi
    
    # JTL format: timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect
    # We need: total_samples, errors_5xx, avg_time, p90, p95, error_rate, throughput
    
    # Count total samples (skip header)
    local total_samples=$(tail -n +2 "${jtl_file}" | wc -l | tr -d ' ')
    
    # Count 5xx errors
    local errors_5xx=$(tail -n +2 "${jtl_file}" | awk -F',' '$4 >= 500 && $4 < 600 {count++} END {print count+0}')
    
    # Count all errors (non-2xx)
    local errors_all=$(tail -n +2 "${jtl_file}" | awk -F',' '$4 < 200 || $4 >= 300 {count++} END {print count+0}')
    
    # Calculate error rate
    local error_rate=0
    if [ "${total_samples}" -gt 0 ]; then
        error_rate=$(awk -v total="${total_samples}" -v err="${errors_all}" 'BEGIN {printf "%.2f", (err/total)*100}')
    fi
    
    # Calculate average response time (column 2: elapsed)
    local avg_time=0
    if [ "${total_samples}" -gt 0 ]; then
        avg_time=$(tail -n +2 "${jtl_file}" | awk -F',' '{sum+=$2; count++} END {if (count > 0) printf "%.0f", sum/count; else print "0"}')
    fi
    
    # Calculate p90 and p95 (need to sort by elapsed time)
    local p90=0
    local p95=0
    if [ "${total_samples}" -gt 0 ]; then
        # Extract elapsed times, sort, calculate percentiles
        # Create temp file for sorted times
        local temp_file=$(mktemp)
        tail -n +2 "${jtl_file}" | awk -F',' '{print $2}' | sort -n > "${temp_file}"
        local count=$(wc -l < "${temp_file}" | tr -d ' ')
        
        # Calculate percentile indices (p90 = 90% of samples, p95 = 95% of samples)
        # Use ceiling to ensure we get the right element
        local p90_idx=$(awk -v count="${count}" 'BEGIN {idx = int(count * 0.9) + (count * 0.9 > int(count * 0.9) ? 1 : 0); if (idx < 1) idx = 1; if (idx > count) idx = count; print idx}')
        local p95_idx=$(awk -v count="${count}" 'BEGIN {idx = int(count * 0.95) + (count * 0.95 > int(count * 0.95) ? 1 : 0); if (idx < 1) idx = 1; if (idx > count) idx = count; print idx}')
        
        p90=$(sed -n "${p90_idx}p" "${temp_file}")
        p95=$(sed -n "${p95_idx}p" "${temp_file}")
        
        # Cleanup
        rm -f "${temp_file}"
    fi
    
    # Calculate throughput (requests per second)
    # Get first and last timestamp
    local first_time=$(tail -n +2 "${jtl_file}" | head -1 | awk -F',' '{print $1}')
    local last_time=$(tail -n +2 "${jtl_file}" | tail -1 | awk -F',' '{print $1}')
    local duration=0
    local throughput=0
    if [ -n "${first_time}" ] && [ -n "${last_time}" ]; then
        duration=$(awk -v first="${first_time}" -v last="${last_time}" 'BEGIN {printf "%.2f", (last - first) / 1000}')
        if (( $(echo "${duration} > 0" | bc -l 2>/dev/null || echo "0") )); then
            throughput=$(awk -v total="${total_samples}" -v dur="${duration}" 'BEGIN {printf "%.2f", total / dur}')
        fi
    fi
    
    echo "${total_samples},${errors_5xx},${errors_all},${error_rate},${avg_time},${p90},${p95},${throughput}"
}

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Load Testing (TestPlan 5.2.6)${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# Предупреждение о подготовке БД
echo -e "${YELLOW}ВАЖНО: Перед запуском Load Testing рекомендуется подготовить БД${NC}"
echo "Для подготовки БД (очистка и заполнение 100 записями) выполните:"
echo "  cd ../scripts"
echo "  ./run-load-test-prepare-db.sh"
echo ""
read -p "Продолжить без подготовки БД? (yes/no): " continue_without_prep
if [[ "$continue_without_prep" != "yes" ]]; then
    echo "Отменено пользователем"
    exit 0
fi
echo ""

# Determine test scenario based on user count and quick test flag
if [ "${QUICK_TEST}" = true ]; then
    # Quick test: 30 seconds total
    RAMP_UP=10
    DURATION=30
    echo -e "${YELLOW}БЫСТРЫЙ ТЕСТ: 30 секунд${NC}"
    echo "Ramp-up: ${RAMP_UP} сек, Duration: ${DURATION} сек"
    P90_THRESHOLD=5000
    P95_THRESHOLD=5000
else
    if [ "${USERS}" -eq 50 ]; then
        echo -e "${YELLOW}Сценарий: 50 пользователей (целевая нагрузка)${NC}"
        echo "Распределение: 20 просмотр, 15 управление, 20 ставки, 5 внешние API"
        echo "Ramp-up: 0 -> 50 за 5 минут (300 сек)"
        echo "Удержание: 30 минут (1800 сек)"
        echo "Критерии успеха: 90% операций ≤5 с, ошибки ≤1%"
        RAMP_UP=300
        DURATION=2100
        P90_THRESHOLD=5000
        P95_THRESHOLD=5000
    elif [ "${USERS}" -eq 100 ]; then
        echo -e "${YELLOW}Сценарий: 100 пользователей (пиковая нагрузка)${NC}"
        echo "Распределение: 40 просмотр, 30 управление, 40 ставки, 10 внешние API"
        echo "Ramp-up: 0 -> 100 за 10 минут (600 сек)"
        echo "Удержание: 30 минут (1800 сек)"
        echo "Критерии успеха: 90% операций ≤8 с, ошибки ≤1%"
        RAMP_UP=600
        DURATION=2400
        P90_THRESHOLD=8000
        P95_THRESHOLD=8000
    else
        echo -e "${YELLOW}Сценарий: ${USERS} пользователей (кастомная нагрузка)${NC}"
        RAMP_UP=300
        DURATION=2100
        P90_THRESHOLD=5000
        P95_THRESHOLD=5000
    fi
fi

# Calculate user distribution
if [ "${USERS}" -eq 50 ]; then
    VIEW_USERS=20
    MANAGE_USERS=15
    BETS_USERS=20
    EXT_USERS=5
elif [ "${USERS}" -eq 100 ]; then
    VIEW_USERS=40
    MANAGE_USERS=30
    BETS_USERS=40
    EXT_USERS=10
else
    # Proportional distribution for custom user count
    VIEW_USERS=$((USERS * 40 / 100))
    MANAGE_USERS=$((USERS * 30 / 100))
    BETS_USERS=$((USERS * 40 / 100))
    EXT_USERS=$((USERS - VIEW_USERS - MANAGE_USERS - BETS_USERS))
fi

echo ""
echo "Test Plan: ${LOAD_TEST_PLAN}"
echo "Results: ${JTL_FILE}"
echo "HTML Report: ${HTML_REPORT_DIR}"
echo ""

# Create temporary test plan for quick test
TEMP_TEST_PLAN=""
if [ "${QUICK_TEST}" = true ]; then
    TEMP_TEST_PLAN="${LOAD_TEST_PLAN}.quick.${TIMESTAMP}"
    cp "${LOAD_TEST_PLAN}" "${TEMP_TEST_PLAN}"
    
    # Update test plan using Python script if available
    UPDATE_SCRIPT="${TEST_PLAN_DIR}/stress-test/update_thread_counts.py"
    if [ -f "${UPDATE_SCRIPT}" ] && command -v python3 &> /dev/null; then
        python3 "${UPDATE_SCRIPT}" "${TEMP_TEST_PLAN}" \
            "${VIEW_USERS}" "${MANAGE_USERS}" "${BETS_USERS}" "${EXT_USERS}" \
            "${RAMP_UP}" "${DURATION}"
        LOAD_TEST_PLAN="${TEMP_TEST_PLAN}"
    else
        # Fallback: use sed (less reliable but works)
        sed -i.bak "s/<intProp name=\"ThreadGroup.ramp_time\">[0-9]*<\/intProp>/<intProp name=\"ThreadGroup.ramp_time\">${RAMP_UP}<\/intProp>/g" "${TEMP_TEST_PLAN}"
        sed -i.bak "s/<longProp name=\"ThreadGroup.duration\">[0-9]*<\/longProp>/<longProp name=\"ThreadGroup.duration\">${DURATION}<\/longProp>/g" "${TEMP_TEST_PLAN}"
        rm -f "${TEMP_TEST_PLAN}.bak"
        LOAD_TEST_PLAN="${TEMP_TEST_PLAN}"
    fi
fi

# Run JMeter in non-GUI mode
echo -e "${YELLOW}Запуск JMeter...${NC}"
START_TIME=$(date +%s)

if jmeter -n -t "${LOAD_TEST_PLAN}" \
    -l "${JTL_FILE}" \
    -e -o "${HTML_REPORT_DIR}" \
    -Jserver_host=localhost \
    -Jserver_port=9314 \
    -q "${TEST_PLAN_DIR}/jmeter.properties" 2>/dev/null || \
   jmeter -n -t "${LOAD_TEST_PLAN}" \
    -l "${JTL_FILE}" \
    -e -o "${HTML_REPORT_DIR}" \
    -Jserver_host=localhost \
    -Jserver_port=9314; then
    
    END_TIME=$(date +%s)
    ELAPSED=$((END_TIME - START_TIME))
    
    echo -e "${GREEN}JMeter тест завершён${NC}"
    echo ""
    
    # Extract metrics
    echo -e "${YELLOW}Извлечение метрик...${NC}"
    METRICS=$(extract_metrics "${JTL_FILE}")
    IFS=',' read -r TOTAL_SAMPLES ERRORS_5XX ERRORS_ALL ERROR_RATE AVG_TIME P90 P95 THROUGHPUT <<< "${METRICS}"
    
    # Check success criteria
    SUCCESS=true
    STATUS_MSG=""
    
    # Check error rate (should be ≤1%)
    if (( $(echo "${ERROR_RATE} > 1.0" | bc -l 2>/dev/null || echo "0") )); then
        SUCCESS=false
        STATUS_MSG="${STATUS_MSG}❌ Процент ошибок ${ERROR_RATE}% > 1% (требование: ≤1%)\n"
    else
        STATUS_MSG="${STATUS_MSG}✓ Процент ошибок: ${ERROR_RATE}% (требование: ≤1%)\n"
    fi
    
    # Check p90 (should be ≤ threshold)
    if [ "${P90}" -gt "${P90_THRESHOLD}" ]; then
        SUCCESS=false
        STATUS_MSG="${STATUS_MSG}❌ p90 время отклика: ${P90} мс > ${P90_THRESHOLD} мс (требование: ≤${P90_THRESHOLD} мс)\n"
    else
        STATUS_MSG="${STATUS_MSG}✓ p90 время отклика: ${P90} мс (требование: ≤${P90_THRESHOLD} мс)\n"
    fi
    
    # Check p95 (should be ≤ threshold)
    if [ "${P95}" -gt "${P95_THRESHOLD}" ]; then
        SUCCESS=false
        STATUS_MSG="${STATUS_MSG}❌ p95 время отклика: ${P95} мс > ${P95_THRESHOLD} мс (требование: ≤${P95_THRESHOLD} мс)\n"
    else
        STATUS_MSG="${STATUS_MSG}✓ p95 время отклика: ${P95} мс (требование: ≤${P95_THRESHOLD} мс)\n"
    fi
    
    # Output results
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}Результаты Load Testing${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo "Общие метрики:"
    echo "  - Всего запросов: ${TOTAL_SAMPLES}"
    echo "  - Ошибки 5xx: ${ERRORS_5XX}"
    echo "  - Всего ошибок: ${ERRORS_ALL}"
    echo "  - Процент ошибок: ${ERROR_RATE}%"
    echo "  - Среднее время отклика: ${AVG_TIME} мс"
    echo "  - p90 время отклика: ${P90} мс"
    echo "  - p95 время отклика: ${P95} мс"
    echo "  - Пропускная способность: ${THROUGHPUT} запросов/сек"
    echo "  - Время выполнения теста: ${ELAPSED} сек"
    echo ""
    echo "Проверка критериев успеха:"
    echo -e "${STATUS_MSG}"
    
    # Save summary to CSV
    echo "Scenario,Users,Total Samples,Errors 5xx,All Errors,Error Rate (%),Avg Time (ms),p90 (ms),p95 (ms),Throughput (req/s),Status" > "${CSV_FILE}"
    STATUS_TEXT="PASSED"
    if [ "${SUCCESS}" = false ]; then
        STATUS_TEXT="FAILED"
    fi
    echo "LOAD-${USERS},${USERS},${TOTAL_SAMPLES},${ERRORS_5XX},${ERRORS_ALL},${ERROR_RATE},${AVG_TIME},${P90},${P95},${THROUGHPUT},${STATUS_TEXT}" >> "${CSV_FILE}"
    
    echo "Детальные результаты:"
    echo "  - JTL файл: ${JTL_FILE}"
    echo "  - HTML отчёт: ${HTML_REPORT_DIR}/index.html"
    echo "  - CSV сводка: ${CSV_FILE}"
    echo ""
    echo "Для просмотра HTML отчёта:"
    echo "  open file://${HTML_REPORT_DIR}/index.html"
    echo ""
    
    # Cleanup temporary test plan
    if [ -n "${TEMP_TEST_PLAN}" ] && [ -f "${TEMP_TEST_PLAN}" ]; then
        rm -f "${TEMP_TEST_PLAN}"
    fi
    
    if [ "${SUCCESS}" = true ]; then
        echo -e "${GREEN}========================================${NC}"
        echo -e "${GREEN}Load Testing: ВСЕ КРИТЕРИИ ВЫПОЛНЕНЫ${NC}"
        echo -e "${GREEN}========================================${NC}"
        exit 0
    else
        echo -e "${RED}========================================${NC}"
        echo -e "${RED}Load Testing: КРИТЕРИИ НЕ ВЫПОЛНЕНЫ${NC}"
        echo -e "${RED}========================================${NC}"
        exit 1
    fi
else
    echo -e "${RED}Load Testing failed!${NC}"
    # Cleanup temporary test plan on error
    if [ -n "${TEMP_TEST_PLAN}" ] && [ -f "${TEMP_TEST_PLAN}" ]; then
        rm -f "${TEMP_TEST_PLAN}"
    fi
    exit 1
fi

