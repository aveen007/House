#!/usr/bin/env bash
set -euo pipefail

DB_CONTAINER="${DB_CONTAINER:-postgres-house}"
DB_NAME="${DB_NAME:-house_db}"
DB_USER="${DB_USER:-postgres}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SQL_FILE="${SCRIPT_DIR}/db-integrity-checks.sql"

if [[ ! -f "$SQL_FILE" ]]; then
  echo "ERROR: SQL file not found: $SQL_FILE"
  exit 1
fi

echo "=========================================="
echo "Database Integrity Testing"
echo "=========================================="
echo "Container: $DB_CONTAINER"
echo "Database: $DB_NAME"
echo "User: $DB_USER"
echo "SQL File: $SQL_FILE"
echo ""

if ! docker exec -i "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -v ON_ERROR_STOP=1 < "$SQL_FILE"; then
  echo ""
  echo "------------------------------------------"
  echo "ERROR: Database integrity test failed!"
  echo "------------------------------------------"
  exit 1
fi

echo ""
echo "------------------------------------------"
echo "SUCCESS: All database integrity checks passed!"
echo "------------------------------------------"

