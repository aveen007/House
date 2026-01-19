#!/bin/bash

# Stress Testing Script
# Runs stress testing with step-wise load increase: 50, 75, 100, 125, 150, 200 users
# Each step: ramp-up 3 minutes, hold 10 minutes

set -euo pipefail

# Configuration
TEST_PLAN_DIR="$(cd "$(dirname "$0")" && pwd)"
RESULTS_DIR="${TEST_PLAN_DIR}/results"
BASE_TEST_PLAN="${TEST_PLAN_DIR}/stress-test/stress_test_plan_base.jmx"
STRESS_TEST_PLAN="${TEST_PLAN_DIR}/stress-test/stress_test_plan.jmx"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Create results directory if it doesn't exist
mkdir -p "${RESULTS_DIR}"

# Check if JMeter is installed
if ! command -v jmeter &> /dev/null; then
    echo -e "${RED}Error: JMeter is not installed or not in PATH${NC}"
    echo "Please install JMeter: brew install jmeter"
    exit 1
fi

# Check if base test plan exists
if [ ! -f "${BASE_TEST_PLAN}" ]; then
    echo -e "${RED}Error: Base test plan not found at ${BASE_TEST_PLAN}${NC}"
    exit 1
fi

# Load levels: total users -> (View, Manage, Bets, External)
# Using function instead of associative array for compatibility
get_load_distribution() {
    local total=$1
    case $total in
        50)  echo "20:15:10:5" ;;
        75)  echo "30:22:15:8" ;;
        100) echo "40:30:20:10" ;;
        125) echo "50:37:25:13" ;;
        150) echo "60:45:30:15" ;;
        200) echo "80:60:40:20" ;;
        *)   echo "0:0:0:0" ;;
    esac
}

# Stress test parameters
RAMP_UP=180      # 3 minutes in seconds
HOLD_TIME=600    # 10 minutes in seconds
DURATION=$((RAMP_UP + HOLD_TIME))  # 13 minutes total per step

TIMESTAMP=$(date +%Y%m%d_%H%M%S)
SUMMARY_FILE="${RESULTS_DIR}/stress_test_summary_${TIMESTAMP}.csv"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Stress Testing - Step-wise Load Increase${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "Test Plan: ${BASE_TEST_PLAN}"
echo "Results Directory: ${RESULTS_DIR}"
echo "Summary File: ${SUMMARY_FILE}"
echo ""
echo "Load Steps: 50, 75, 100, 125, 150, 200 users"
echo "Each step: Ramp-up ${RAMP_UP}s (3 min), Hold ${HOLD_TIME}s (10 min)"
echo "Total time per step: ${DURATION}s (~13 min)"
echo "Estimated total time: ~78 minutes (6 steps × 13 min)"
echo ""
echo -e "${YELLOW}WARNING: This test will run for a long time!${NC}"
echo -e "${YELLOW}Make sure the server is running and monitor system resources.${NC}"
echo ""
read -p "Press Enter to continue or Ctrl+C to cancel..."

# Create summary CSV header
echo "Step,TotalUsers,ViewUsers,ManageUsers,BetsUsers,ExtUsers,StartTime,EndTime,TotalSamples,ErrorRate,AvgResponseTime,p90,p95,p99,Status" > "${SUMMARY_FILE}"

# Function to update thread counts in test plan using Python
update_thread_counts() {
    local total_users=$1
    local view_users=$2
    local manage_users=$3
    local bets_users=$4
    local ext_users=$5
    
    # Copy base test plan
    cp "${BASE_TEST_PLAN}" "${STRESS_TEST_PLAN}"
    
    # Update using Python script
    local update_script="${TEST_PLAN_DIR}/stress-test/update_thread_counts.py"
    if [ -f "${update_script}" ] && command -v python3 &> /dev/null; then
        python3 "${update_script}" "${STRESS_TEST_PLAN}" \
            "${view_users}" "${manage_users}" "${bets_users}" "${ext_users}" \
            "${RAMP_UP}" "${DURATION}"
    else
        echo -e "${YELLOW}Warning: Python script not found, using fallback method${NC}"
        # Fallback: basic sed replacement (less reliable)
        sed -i.bak "s/<intProp name=\"ThreadGroup.num_threads\">20<\/intProp>/<intProp name=\"ThreadGroup.num_threads\">${view_users}<\/intProp>/" "${STRESS_TEST_PLAN}" || true
        sed -i.bak "s/<intProp name=\"ThreadGroup.num_threads\">15<\/intProp>/<intProp name=\"ThreadGroup.num_threads\">${manage_users}<\/intProp>/" "${STRESS_TEST_PLAN}" || true
        sed -i.bak "s/<intProp name=\"ThreadGroup.num_threads\">10<\/intProp>/<intProp name=\"ThreadGroup.num_threads\">${bets_users}<\/intProp>/" "${STRESS_TEST_PLAN}" || true
        sed -i.bak "s/<intProp name=\"ThreadGroup.num_threads\">5<\/intProp>/<intProp name=\"ThreadGroup.num_threads\">${ext_users}<\/intProp>/" "${STRESS_TEST_PLAN}" || true
        sed -i.bak "s/<intProp name=\"ThreadGroup.ramp_time\">[0-9]*<\/intProp>/<intProp name=\"ThreadGroup.ramp_time\">${RAMP_UP}<\/intProp>/g" "${STRESS_TEST_PLAN}"
        sed -i.bak "s/<longProp name=\"ThreadGroup.duration\">[0-9]*<\/longProp>/<longProp name=\"ThreadGroup.duration\">${DURATION}<\/longProp>/g" "${STRESS_TEST_PLAN}"
        rm -f "${STRESS_TEST_PLAN}.bak"
    fi
}

# Function to extract metrics from JMeter HTML report
extract_metrics() {
    local report_dir=$1
    local jtl_file=$2
    
    # Use JMeter's Aggregate Report or parse JTL file
    # For simplicity, we'll use basic stats from JTL
    if [ -f "${jtl_file}" ]; then
        # Count total samples
        local total_samples=$(wc -l < "${jtl_file}" | tr -d ' ')
        # Count errors (non-200 status codes)
        local errors=$(awk -F',' '$8 != "200" && $8 != "201" {count++} END {print count+0}' "${jtl_file}")
        local error_rate=$(awk -v total="${total_samples}" -v err="${errors}" 'BEGIN {if (total > 0) printf "%.2f", (err/total)*100; else print "0.00"}')
        
        # Calculate average response time (column 2 in JTL)
        local avg_time=$(awk -F',' 'NR>1 {sum+=$2; count++} END {if (count > 0) printf "%.0f", sum/count; else print "0"}' "${jtl_file}")
        
        echo "${total_samples},${error_rate},${avg_time}"
    else
        echo "0,0.00,0"
    fi
}

# Run stress test for each load level
STEP=1
for TOTAL_USERS in 50 75 100 125 150 200; do
    DISTRIBUTION=$(get_load_distribution ${TOTAL_USERS})
    IFS=':' read -r VIEW_USERS MANAGE_USERS BETS_USERS EXT_USERS <<< "${DISTRIBUTION}"
    
    echo ""
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}Step ${STEP}/6: ${TOTAL_USERS} users${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo "Distribution: View=${VIEW_USERS}, Manage=${MANAGE_USERS}, Bets=${BETS_USERS}, Ext=${EXT_USERS}"
    echo "Ramp-up: ${RAMP_UP}s, Hold: ${HOLD_TIME}s, Total: ${DURATION}s"
    echo ""
    
    # Update test plan with current load level
    update_thread_counts "${TOTAL_USERS}" "${VIEW_USERS}" "${MANAGE_USERS}" "${BETS_USERS}" "${EXT_USERS}"
    
    # Prepare output files
    STEP_JTL="${RESULTS_DIR}/stress_step_${TOTAL_USERS}users_${TIMESTAMP}.jtl"
    STEP_REPORT="${RESULTS_DIR}/stress_step_${TOTAL_USERS}users_report_${TIMESTAMP}"
    
    # Remove old files if they exist
    rm -f "${STEP_JTL}"
    rm -rf "${STEP_REPORT}"
    
    START_TIME=$(date +%s)
    START_TIME_STR=$(date '+%Y-%m-%d %H:%M:%S')
    
    echo -e "${YELLOW}Starting test at ${START_TIME_STR}...${NC}"
    
    # Run JMeter
    if jmeter -n -t "${STRESS_TEST_PLAN}" \
        -l "${STEP_JTL}" \
        -e -o "${STEP_REPORT}" \
        -Jserver_host=localhost \
        -Jserver_port=9314 \
        -q "${TEST_PLAN_DIR}/jmeter.properties" 2>/dev/null || \
       jmeter -n -t "${STRESS_TEST_PLAN}" \
        -l "${STEP_JTL}" \
        -e -o "${STEP_REPORT}" \
        -Jserver_host=localhost \
        -Jserver_port=9314; then
        
        END_TIME=$(date +%s)
        END_TIME_STR=$(date '+%Y-%m-%d %H:%M:%S')
        ELAPSED=$((END_TIME - START_TIME))
        
        # Extract metrics
        METRICS=$(extract_metrics "${STEP_REPORT}" "${STEP_JTL}")
        IFS=',' read -r TOTAL_SAMPLES ERROR_RATE AVG_TIME <<< "${METRICS}"
        
        # Check for degradation (5xx errors > 5% or p99 > 30s)
        # For now, we'll use error rate as indicator
        if (( $(echo "${ERROR_RATE} > 5.0" | bc -l 2>/dev/null || echo "0") )); then
            STATUS="DEGRADATION"
            echo -e "${RED}⚠️  DEGRADATION DETECTED: Error rate ${ERROR_RATE}% > 5%${NC}"
        else
            STATUS="OK"
            echo -e "${GREEN}✓ Step completed successfully${NC}"
        fi
        
        # Write to summary
        echo "${STEP},${TOTAL_USERS},${VIEW_USERS},${MANAGE_USERS},${BETS_USERS},${EXT_USERS},${START_TIME_STR},${END_TIME_STR},${TOTAL_SAMPLES},${ERROR_RATE},${AVG_TIME},N/A,N/A,N/A,${STATUS}" >> "${SUMMARY_FILE}"
        
        echo "Results: ${STEP_JTL}"
        echo "Report: ${STEP_REPORT}/index.html"
        echo "Elapsed: ${ELAPSED}s"
        echo ""
        
        # If degradation detected, ask if user wants to continue
        if [ "${STATUS}" = "DEGRADATION" ]; then
            echo -e "${YELLOW}Degradation point identified at ${TOTAL_USERS} users.${NC}"
            read -p "Continue to next step? (y/n): " -n 1 -r
            echo
            if [[ ! $REPLY =~ ^[Yy]$ ]]; then
                echo "Stopping stress test at user's request."
                break
            fi
        fi
    else
        echo -e "${RED}✗ Step failed!${NC}"
        echo "${STEP},${TOTAL_USERS},${VIEW_USERS},${MANAGE_USERS},${BETS_USERS},${EXT_USERS},${START_TIME_STR},ERROR,0,100.00,0,N/A,N/A,N/A,FAILED" >> "${SUMMARY_FILE}"
        break
    fi
    
    STEP=$((STEP + 1))
    
    # Small delay between steps
    sleep 5
done

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Stress Testing Completed${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "Summary file: ${SUMMARY_FILE}"
echo ""
echo "To view detailed reports, check:"
echo "  ${RESULTS_DIR}/stress_step_*_report_${TIMESTAMP}/"
echo ""
