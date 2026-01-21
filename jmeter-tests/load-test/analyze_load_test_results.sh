#!/usr/bin/env bash
set -euo pipefail

# Скрипт для анализа результатов Load Testing
# Анализирует CSV файлы с результатами JMeter и создает сводку

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Анализ результатов Load Testing${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# Функция для извлечения метрик из CSV
extract_metrics() {
    local file=$1
    local label=$2
    
    if [ ! -f "$file" ]; then
        echo "N/A"
        return
    fi
    
    # CSV формат: Label,# Samples,Average,Median,90% Line,95% Line,99% Line,Min,Max,Error %,Throughput
    awk -F',' -v label="$label" '
        BEGIN { found=0 }
        $1 == label { 
            printf "%s|%s|%s|%s|%s|%s", $2, $3, $5, $6, $10, $11
            found=1
            exit
        }
        END { if (!found) print "N/A" }
    ' "$file"
}

# Функция для определения статуса на основе метрик
get_status() {
    local p90=$1
    local error_rate=$2
    
    # Удаляем % из error_rate если есть
    error_rate=$(echo "$error_rate" | sed 's/%//')
    
    if (( $(echo "$error_rate > 5" | bc -l 2>/dev/null || echo "0") )); then
        echo -e "${RED}FAIL${NC}"
    elif (( $(echo "$p90 > 5000" | bc -l 2>/dev/null || echo "0") )); then
        echo -e "${YELLOW}WARN${NC}"
    else
        echo -e "${GREEN}PASS${NC}"
    fi
}

echo -e "${BLUE}=== Результаты по конфигурациям БД ===${NC}"
echo ""

# avgdb (100 пациентов, 100 визитов, 100 ставок)
echo -e "${YELLOW}avgdb (100 пациентов, 100 визитов, 100 ставок)${NC}"
echo "┌─────────┬──────────┬──────────┬──────────┬──────────┬──────────┬──────────┐"
echo "│ Users   │ Samples  │ Avg (ms)  │ P90 (ms)  │ P95 (ms)  │ Error %   │ Status   │"
echo "├─────────┼──────────┼──────────┼──────────┼──────────┼──────────┼──────────┤"

for users in 40 80 160 320; do
    file="${SCRIPT_DIR}/load_test_avgdb_${users}users.csv"
    if [ -f "$file" ]; then
        metrics=$(extract_metrics "$file" "TOTAL")
        if [ "$metrics" != "N/A" ]; then
            IFS='|' read -r samples avg p90 p95 error_rate throughput <<< "$metrics"
            status=$(get_status "$p90" "$error_rate")
            printf "│ %-7s │ %-8s │ %-8s │ %-8s │ %-8s │ %-8s │ %-8s │\n" \
                "$users" "$samples" "$avg" "$p90" "$p95" "$error_rate" "$status"
        fi
    fi
done
echo "└─────────┴──────────┴──────────┴──────────┴──────────┴──────────┴──────────┘"
echo ""

# halfmax_db (18155 пациентов, 20100 визитов, 21114 ставок)
echo -e "${YELLOW}halfmax_db (18155 пациентов, 20100 визитов, 21114 ставок)${NC}"
echo "┌─────────┬──────────┬──────────┬──────────┬──────────┬──────────┬──────────┐"
echo "│ Users   │ Samples  │ Avg (ms)  │ P90 (ms)  │ P95 (ms)  │ Error %   │ Status   │"
echo "├─────────┼──────────┼──────────┼──────────┼──────────┼──────────┼──────────┤"

for users in 320; do
    file="${SCRIPT_DIR}/load_test_halfmax_db_${users}users.csv"
    if [ -f "$file" ]; then
        metrics=$(extract_metrics "$file" "TOTAL")
        if [ "$metrics" != "N/A" ]; then
            IFS='|' read -r samples avg p90 p95 error_rate throughput <<< "$metrics"
            status=$(get_status "$p90" "$error_rate")
            printf "│ %-7s │ %-8s │ %-8s │ %-8s │ %-8s │ %-8s │ %-8s │\n" \
                "$users" "$samples" "$avg" "$p90" "$p95" "$error_rate" "$status"
        fi
    fi
done
echo "└─────────┴──────────┴──────────┴──────────┴──────────┴──────────┴──────────┘"
echo ""

# max_db (21062 пациента, 40001 визит, 40001 ставка)
echo -e "${YELLOW}max_db (21062 пациента, 40001 визит, 40001 ставка)${NC}"
echo "┌─────────┬──────────┬──────────┬──────────┬──────────┬──────────┬──────────┐"
echo "│ Users   │ Samples  │ Avg (ms)  │ P90 (ms)  │ P95 (ms)  │ Error %   │ Status   │"
echo "├─────────┼──────────┼──────────┼──────────┼──────────┼──────────┼──────────┤"

for users in 40; do
    file="${SCRIPT_DIR}/load_test_max_db_${users}users.csv"
    if [ -f "$file" ]; then
        metrics=$(extract_metrics "$file" "TOTAL")
        if [ "$metrics" != "N/A" ]; then
            IFS='|' read -r samples avg p90 p95 error_rate throughput <<< "$metrics"
            status=$(get_status "$p90" "$error_rate")
            printf "│ %-7s │ %-8s │ %-8s │ %-8s │ %-8s │ %-8s │ %-8s │\n" \
                "$users" "$samples" "$avg" "$p90" "$p95" "$error_rate" "$status"
        fi
    fi
done
echo "└─────────┴──────────┴──────────┴──────────┴──────────┴──────────┴──────────┘"
echo ""

# Детальный анализ по операциям для avgdb 320 users (критический случай)
echo -e "${BLUE}=== Детальный анализ: avgdb 320 users ===${NC}"
file="${SCRIPT_DIR}/load_test_avgdb_320users.csv"
if [ -f "$file" ]; then
    echo ""
    echo "Операция                    │ Samples │ Avg (ms) │ P90 (ms) │ P95 (ms) │ Error %"
    echo "────────────────────────────┼─────────┼──────────┼──────────┼──────────┼─────────"
    
    while IFS=',' read -r label samples avg median p90 p95 p99 min max error_rate throughput rest; do
        if [ "$label" != "Label" ] && [ "$label" != "TOTAL" ]; then
            printf "%-27s │ %-7s │ %-8s │ %-8s │ %-8s │ %-8s\n" \
                "$label" "$samples" "$avg" "$p90" "$p95" "$error_rate"
        fi
    done < "$file"
    echo ""
fi

# Выводы и рекомендации
echo -e "${BLUE}=== Выводы и рекомендации ===${NC}"
echo ""
echo -e "${GREEN}✓ avgdb (100 записей):${NC}"
echo "  - 40-80 users: Отличная производительность (0% ошибок, низкие задержки)"
echo "  - 160 users: Приемлемая производительность (0% ошибок, задержки растут)"
echo "  - 320 users: Проблемы с производительностью (p90 > 25s, но 0% ошибок)"
echo ""
echo -e "${YELLOW}⚠ halfmax_db (18K+ записей):${NC}"
echo "  - 320 users: Критические проблемы (43.9% ошибок, очень высокие задержки)"
echo "  - Рекомендация: Уменьшить количество пользователей или оптимизировать БД"
echo ""
echo -e "${RED}✗ max_db (40K+ записей):${NC}"
echo "  - 40 users: Проблемы с производительностью (1.9% ошибок, высокие задержки)"
echo "  - Особенно страдают: Get All Patients, Create Patient"
echo "  - Рекомендация: Требуется оптимизация запросов и индексов БД"
echo ""
echo -e "${BLUE}Общие рекомендации:${NC}"
echo "  1. Для нормальной работы рекомендуется avgdb (100 записей) с до 160 пользователей"
echo "  2. При увеличении объема данных требуется оптимизация БД (индексы, кэширование)"
echo "  3. Критическая операция: Get All Patients - требует пагинации или кэширования"
echo ""

