#!/usr/bin/env bash
set -euo pipefail

# Скрипт для запуска Volume Testing согласно TestPlan 5.2.8

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Volume Testing (TestPlan 5.2.8)${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# Конфигурация БД
DB_CONTAINER="${DB_CONTAINER:-postgres-house}"
DB_NAME="${DB_NAME:-house_db}"
DB_USER="${DB_USER:-postgres}"

# Проверка доступности БД через Docker
echo -e "${YELLOW}Проверка доступности БД...${NC}"
if ! docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -c "SELECT 1;" > /dev/null 2>&1; then
    echo -e "${RED}ОШИБКА: БД недоступна${NC}"
    echo "Проверьте:"
    echo "  - Контейнер запущен: docker ps | grep $DB_CONTAINER"
    echo "  - Настройки: DB_CONTAINER=$DB_CONTAINER, DB_NAME=$DB_NAME, DB_USER=$DB_USER"
    exit 1
fi
echo -e "${GREEN}БД доступна${NC}"
echo ""

# Шаг 1: Наполнение БД (опционально, если данные уже есть)
if [ "${SKIP_POPULATE:-false}" != "true" ]; then
    echo -e "${YELLOW}Шаг 1: Наполнение БД большими объёмами данных${NC}"
    echo "ВНИМАНИЕ: Это создаст 10,000 пациентов, 20,000 ставок, 20,000 анализов"
    read -p "Продолжить наполнение БД? (yes/no): " confirm
    
    if [ "$confirm" = "yes" ]; then
        echo "Выполнение SQL скрипта для наполнения БД..."
        docker exec -i "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" < "${SCRIPT_DIR}/volume-test-populate-db.sql"
        echo -e "${GREEN}Наполнение БД завершено${NC}"
    else
        echo "Наполнение БД пропущено"
    fi
    echo ""
fi

# Шаг 2: Запуск JUnit тестов
echo -e "${YELLOW}Шаг 2: Запуск JUnit тестов Volume Testing${NC}"
cd "$PROJECT_DIR"
if ! mvn test -Dtest=VolumeTestingTest 2>&1 | tee /tmp/volume_test.log; then
    echo -e "${RED}ОШИБКА: Тесты провалились${NC}"
    exit 1
fi
echo -e "${GREEN}JUnit тесты завершены${NC}"
echo ""

# Итоговая сводка
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Volume Testing завершён${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "Результаты:"
echo "  - Наполнение БД: Выполнено (если не пропущено)"
echo "  - JUnit тесты: Завершены"
echo ""
echo "Детальная информация:"
echo "  - Лог тестов: /tmp/volume_test.log"
echo "  - SQL скрипт: ${SCRIPT_DIR}/volume-test-populate-db.sql"
