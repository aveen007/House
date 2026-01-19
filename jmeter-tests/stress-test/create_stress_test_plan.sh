#!/bin/bash

# Create stress test plan by copying and modifying base test plan
# This script creates a simplified stress test that runs sequentially

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BASE_PLAN="${SCRIPT_DIR}/stress_test_plan_base.jmx"
OUTPUT_PLAN="${SCRIPT_DIR}/stress_test_plan.jmx"

if [ ! -f "${BASE_PLAN}" ]; then
    echo "Error: Base test plan not found: ${BASE_PLAN}"
    exit 1
fi

echo "Creating stress test plan from base..."
echo "Note: For full step-wise stress testing, it's recommended to create"
echo "      the test plan manually in JMeter GUI with multiple ThreadGroups"
echo "      for each load level (50, 75, 100, 125, 150, 200 users)."
echo ""
echo "This script creates a simplified version that can be run multiple times"
echo "with different user counts using the run_stress_test.sh script."
echo ""

# For now, just copy the base plan
# The run_stress_test.sh script will modify it dynamically
cp "${BASE_PLAN}" "${OUTPUT_PLAN}"

echo "Stress test plan created: ${OUTPUT_PLAN}"
echo ""
echo "To run stress testing:"
echo "  ./run_stress_test.sh"
echo ""
echo "Or manually create a test plan in JMeter GUI with:"
echo "  - Multiple ThreadGroups for each load level"
echo "  - Each ThreadGroup: ramp-up 180s, duration 780s"
echo "  - Load levels: 50, 75, 100, 125, 150, 200 users"

