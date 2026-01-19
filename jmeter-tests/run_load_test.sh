#!/bin/bash

# Load Testing Script
# This script runs Load Testing with JMeter in non-GUI mode

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
    echo -e "${YELLOW}Warning: Test plan not found at ${LOAD_TEST_PLAN}${NC}"
    echo "Please create the test plan using JMeter GUI (see JMETER_TEST_CREATION_GUIDE.md)"
    exit 1
fi

# Get user count (default: 50)
USERS=${1:-50}
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
JTL_FILE="${RESULTS_DIR}/load_test_${USERS}_users_${TIMESTAMP}.jtl"
HTML_REPORT_DIR="${RESULTS_DIR}/load_test_${USERS}_users_report_${TIMESTAMP}"

echo -e "${GREEN}Starting Load Testing with ${USERS} users...${NC}"
echo "Test Plan: ${LOAD_TEST_PLAN}"
echo "Results: ${JTL_FILE}"
echo "HTML Report: ${HTML_REPORT_DIR}"
echo ""

# Run JMeter in non-GUI mode
jmeter -n -t "${LOAD_TEST_PLAN}" \
    -Jusers=${USERS} \
    -l "${JTL_FILE}" \
    -e -o "${HTML_REPORT_DIR}" \
    -Jserver_host=localhost \
    -Jserver_port=8080

if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}Load Testing completed successfully!${NC}"
    echo "Results saved to: ${JTL_FILE}"
    echo "HTML Report: ${HTML_REPORT_DIR}/index.html"
    echo ""
    echo "To view the report, open: file://${HTML_REPORT_DIR}/index.html"
else
    echo -e "${RED}Load Testing failed!${NC}"
    exit 1
fi

