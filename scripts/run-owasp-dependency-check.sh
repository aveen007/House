#!/usr/bin/env bash
set -euo pipefail

# Скрипт для автоматизированного сканирования уязвимостей SAST (OWASP Dependency Check)
# Согласно TestPlan 5.2.9, пункт 4

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
REPORT_DIR="${PROJECT_DIR}/security-reports"
DEPENDENCY_CHECK_VERSION="${DEPENDENCY_CHECK_VERSION:-9.0.9}"

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}OWASP Dependency Check${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# Проверка наличия OWASP Dependency Check
DEPENDENCY_CHECK_CMD=""
if command -v dependency-check.sh &> /dev/null; then
    DEPENDENCY_CHECK_CMD="dependency-check.sh"
elif command -v dependency-check &> /dev/null; then
    DEPENDENCY_CHECK_CMD="dependency-check"
else
    echo -e "${YELLOW}OWASP Dependency Check не найден в PATH${NC}"
    echo "Попытка загрузить через Maven plugin..."
    
    # Используем Maven plugin для OWASP Dependency Check
    cd "$PROJECT_DIR"
    
    if [[ ! -f "pom.xml" ]]; then
        echo -e "${RED}ОШИБКА: pom.xml не найден${NC}"
        exit 1
    fi
    
    # Проверяем, есть ли плагин в pom.xml
    if ! grep -q "dependency-check-maven" pom.xml; then
        echo -e "${YELLOW}Добавляю OWASP Dependency Check Maven plugin...${NC}"
        # Добавляем плагин в pom.xml (в секцию <build><plugins>)
        # Это упрощенная версия - в реальности нужно правильно вставить в XML
        echo -e "${YELLOW}ВНИМАНИЕ: Нужно вручную добавить плагин в pom.xml${NC}"
        echo ""
        echo "Добавьте в pom.xml в секцию <build><plugins>:"
        echo ""
        cat <<'EOF'
        <plugin>
            <groupId>org.owasp</groupId>
            <artifactId>dependency-check-maven</artifactId>
            <version>9.0.9</version>
            <executions>
                <execution>
                    <goals>
                        <goal>check</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
EOF
        echo ""
        echo "Или запустите вручную:"
        echo "  mvn org.owasp:dependency-check-maven:check"
        echo ""
        exit 1
    fi
    
    echo -e "${GREEN}Запускаю OWASP Dependency Check через Maven...${NC}"
    mvn org.owasp:dependency-check-maven:check -Dformat=ALL -DoutputDirectory="${REPORT_DIR}/dependency-check"
    
    if [[ $? -eq 0 ]]; then
        echo -e "${GREEN}✓ Сканирование завершено успешно${NC}"
        echo ""
        echo "Отчеты сохранены в: ${REPORT_DIR}/dependency-check"
        echo ""
        echo "Проверьте отчеты на наличие критических уязвимостей (CVE)."
        exit 0
    else
        echo -e "${RED}ОШИБКА: Сканирование завершилось с ошибками${NC}"
        exit 1
    fi
fi

# Создаем директорию для отчетов
mkdir -p "$REPORT_DIR"

echo -e "${GREEN}Запускаю OWASP Dependency Check...${NC}"
cd "$PROJECT_DIR"

# Запускаем сканирование
"$DEPENDENCY_CHECK_CMD" \
    --project "Medical House System" \
    --scan "$PROJECT_DIR" \
    --format "ALL" \
    --out "$REPORT_DIR/dependency-check" \
    --enableExperimental \
    --failOnCVSS 9.0

if [[ $? -eq 0 ]]; then
    echo -e "${GREEN}✓ Сканирование завершено успешно${NC}"
    echo ""
    echo "Отчеты сохранены в: ${REPORT_DIR}/dependency-check"
    echo ""
    echo "Проверьте отчеты на наличие критических уязвимостей (CVE)."
    echo "Критерий успеха (TestPlan 5.2.9): Отчёты по зависимостям не содержат критических CVE."
else
    echo -e "${RED}ОШИБКА: Сканирование завершилось с ошибками${NC}"
    exit 1
fi

