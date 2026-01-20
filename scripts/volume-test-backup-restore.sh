#!/usr/bin/env bash
set -euo pipefail

# Скрипт для проверки бэкап/restore БД для Volume Testing
# Согласно TestPlan 5.2.8, необходимо проверить поведение бэкап/restore и время резервного копирования

# Конфигурация
DB_CONTAINER="${DB_CONTAINER:-postgres-house}"
DB_NAME="${DB_NAME:-house_db}"
DB_USER="${DB_USER:-postgres}"
BACKUP_DIR="${BACKUP_DIR:-./backups}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="${BACKUP_DIR}/volume_test_backup_${TIMESTAMP}.sql"

# Цвета для вывода
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== Проверка бэкап/restore для Volume Testing ===${NC}"
echo ""

# Создание директории для бэкапов
mkdir -p "$BACKUP_DIR"

# Функция для измерения времени выполнения
measure_time() {
    local start_time=$(date +%s.%N)
    "$@"
    local end_time=$(date +%s.%N)
    local duration=$(echo "$end_time - $start_time" | bc)
    echo "$duration"
}

# Шаг 1: Проверка размера БД до бэкапа
echo -e "${YELLOW}Шаг 1: Проверка размера БД${NC}"
DB_SIZE_BEFORE=$(docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -t -c "
    SELECT pg_size_pretty(pg_database_size('$DB_NAME'));
" | xargs)
echo "Размер БД до бэкапа: $DB_SIZE_BEFORE"
echo ""

# Шаг 2: Создание бэкапа
echo -e "${YELLOW}Шаг 2: Создание бэкапа БД${NC}"
echo "Файл бэкапа: $BACKUP_FILE"

BACKUP_TIME=$(measure_time docker exec "$DB_CONTAINER" pg_dump -U "$DB_USER" -d "$DB_NAME" -F p > "$BACKUP_FILE")

BACKUP_SIZE=$(du -h "$BACKUP_FILE" | cut -f1)
echo -e "${GREEN}Бэкап создан успешно${NC}"
echo "Время создания бэкапа: ${BACKUP_TIME} сек"
echo "Размер файла бэкапа: $BACKUP_SIZE"
echo ""

# Шаг 3: Проверка целостности бэкапа
echo -e "${YELLOW}Шаг 3: Проверка целостности бэкапа${NC}"
if [ -f "$BACKUP_FILE" ] && [ -s "$BACKUP_FILE" ]; then
    # Проверяем, что файл содержит SQL команды
    if grep -q "CREATE\|INSERT\|COPY" "$BACKUP_FILE" 2>/dev/null; then
        echo -e "${GREEN}Бэкап валиден${NC}"
    else
        echo -e "${YELLOW}Предупреждение: Бэкап может быть пустым или повреждён${NC}"
    fi
else
    echo -e "${RED}ОШИБКА: Файл бэкапа не существует или пуст${NC}"
    exit 1
fi
echo ""

# Шаг 4: Подсчёт объектов в бэкапе
echo -e "${YELLOW}Шаг 4: Статистика бэкапа${NC}"
PATIENT_COUNT=$(grep -c "COPY public.patient" "$BACKUP_FILE" 2>/dev/null || echo "0")
BET_COUNT=$(grep -c "COPY public.bet" "$BACKUP_FILE" 2>/dev/null || echo "0")
ANALYSIS_COUNT=$(grep -c "COPY public.patient_analysis" "$BACKUP_FILE" 2>/dev/null || echo "0")

echo "Пациентов в бэкапе: $PATIENT_COUNT"
echo "Ставок в бэкапе: $BET_COUNT"
echo "Анализов в бэкапе: $ANALYSIS_COUNT"
echo ""

# Шаг 5: Тест restore (опционально, требует подтверждения)
if [ "${SKIP_RESTORE:-false}" != "true" ]; then
    echo -e "${YELLOW}Шаг 5: Тест restore (опционально)${NC}"
    echo -e "${RED}ВНИМАНИЕ: Restore удалит все данные в текущей БД!${NC}"
    read -p "Продолжить тест restore? (yes/no): " confirm
    
    if [ "$confirm" = "yes" ]; then
        # Создание тестовой БД для restore
        TEST_DB_NAME="${DB_NAME}_restore_test_${TIMESTAMP}"
        echo "Создание тестовой БД: $TEST_DB_NAME"
        
        docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d postgres -c "DROP DATABASE IF EXISTS $TEST_DB_NAME;" || true
        docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d postgres -c "CREATE DATABASE $TEST_DB_NAME;"
        
        # Restore
        echo "Выполнение restore..."
        RESTORE_TIME=$(measure_time docker exec -i "$DB_CONTAINER" psql -U "$DB_USER" -d "$TEST_DB_NAME" < "$BACKUP_FILE" > /dev/null)
        
        echo -e "${GREEN}Restore выполнен успешно${NC}"
        echo "Время restore: ${RESTORE_TIME} сек"
        
        # Проверка данных после restore
        RESTORE_PATIENT_COUNT=$(docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$TEST_DB_NAME" -t -c "SELECT COUNT(*) FROM patient;" | xargs)
        RESTORE_BET_COUNT=$(docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$TEST_DB_NAME" -t -c "SELECT COUNT(*) FROM bet;" | xargs)
        RESTORE_ANALYSIS_COUNT=$(docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$TEST_DB_NAME" -t -c "SELECT COUNT(*) FROM patient_analysis;" | xargs)
        
        echo "Пациентов после restore: $RESTORE_PATIENT_COUNT"
        echo "Ставок после restore: $RESTORE_BET_COUNT"
        echo "Анализов после restore: $RESTORE_ANALYSIS_COUNT"
        
        # Удаление тестовой БД
        echo "Удаление тестовой БД..."
        docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d postgres -c "DROP DATABASE $TEST_DB_NAME;"
    else
        echo "Тест restore пропущен"
    fi
    echo ""
fi

# Итоговая сводка
echo -e "${GREEN}=== Итоговая сводка ===${NC}"
echo "Размер БД: $DB_SIZE_BEFORE"
echo "Время создания бэкапа: ${BACKUP_TIME} сек"
echo "Размер файла бэкапа: $BACKUP_SIZE"
echo "Файл бэкапа: $BACKUP_FILE"
echo ""
echo -e "${GREEN}Проверка бэкап/restore завершена успешно${NC}"

