#!/usr/bin/env bash
set -euo pipefail

# Скрипт для подготовки БД перед Load Testing
# Очищает таблицы и заполняет их небольшим количеством данных (до 100 записей)

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

# Параметры подключения к БД (можно переопределить через переменные окружения)
DB_CONTAINER="${DB_CONTAINER:-postgres-house}"
DB_NAME="${DB_NAME:-house_db}"
DB_USER="${DB_USER:-postgres}"

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Подготовка БД для Load Testing${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# Проверка доступности PostgreSQL контейнера
echo -e "${YELLOW}Проверка доступности PostgreSQL...${NC}"
if ! docker ps | grep -q "$DB_CONTAINER"; then
    echo -e "${RED}ОШИБКА: Контейнер PostgreSQL '$DB_CONTAINER' не запущен${NC}"
    echo "Запустите контейнер PostgreSQL и повторите попытку"
    exit 1
fi
echo -e "${GREEN}Контейнер PostgreSQL доступен${NC}"
echo ""

# Предупреждение о том, что данные будут удалены
echo -e "${YELLOW}ВНИМАНИЕ: Этот скрипт удалит все данные из следующих таблиц:${NC}"
echo "  - patients"
echo "  - visits"
echo "  - bets"
echo "  - contracts"
echo "  - patient_analysis"
echo "  - analysis_bet"
echo "  - analysis_result"
echo "  - fin_bet"
echo ""
echo -e "${YELLOW}После очистки будут созданы:${NC}"
echo "  - 100 пациентов"
echo "  - 100 визитов"
echo "  - 100 ставок"
echo ""

read -p "Продолжить? (yes/no): " confirm
if [[ "$confirm" != "yes" ]]; then
    echo "Отменено пользователем"
    exit 0
fi

echo ""
echo -e "${YELLOW}Выполнение SQL скрипта...${NC}"

# Выполнение SQL скрипта с проверкой ошибок
SQL_OUTPUT=$(docker exec -i "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" < "${SCRIPT_DIR}/load-test-prepare-db.sql" 2>&1)
SQL_EXIT_CODE=$?

# Проверка на наличие ERROR в выводе
if echo "$SQL_OUTPUT" | grep -qi "ERROR:"; then
    echo ""
    echo -e "${RED}ОШИБКА: При выполнении SQL скрипта произошла ошибка${NC}"
    echo ""
    echo "$SQL_OUTPUT" | grep -i "ERROR:"
    echo ""
    exit 1
fi

# Проверка кода выхода
if [ $SQL_EXIT_CODE -ne 0 ]; then
    echo ""
    echo -e "${RED}ОШИБКА: Не удалось выполнить SQL скрипт (код выхода: $SQL_EXIT_CODE)${NC}"
    echo ""
    echo "$SQL_OUTPUT"
    exit 1
fi

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}БД успешно подготовлена для Load Testing${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "Теперь можно запускать JMeter Load Test:"
echo "  cd jmeter-tests"
echo "  ./run_load_test.sh"
echo ""

