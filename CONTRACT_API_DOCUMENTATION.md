# Contract API Documentation

## Обзор
API для управления контрактами пациентов с медицинскими учреждениями. Включает функционал создания, просмотра, редактирования и подписания контрактов.

## Base URL
```
http://localhost:8080/api/contracts
```

## Endpoints

### 1. Получить все условия (Terms & Conditions)
```http
GET /api/contracts/terms
```

**Описание:** Возвращает список всех доступных шаблонов условий договора.

**Response:**
```json
[
  {
    "termsId": 1,
    "title": "Base Medical Service Contract v1",
    "content": "This contract defines the terms...",
    "version": 1,
    "isActive": true,
    "createdAt": "2025-12-22T06:22:30.273759Z"
  }
]
```

---

### 2. Создать контракт
```http
POST /api/contracts
```

**Описание:** Создает новый контракт для пациента в статусе DRAFT.

**Request Body:**
```json
{
  "patientId": 7,
  "termsId": 1
}
```

**Response:**
```json
{
  "contractId": 3,
  "patientId": 7,
  "termsId": 1,
  "termsVersion": 1,
  "termsTitle": "Base Medical Service Contract v1",
  "termsSnapshot": "This contract defines...",
  "status": "DRAFT",
  "createdAt": "2025-12-24T09:51:27.473766+03:00",
  "updatedAt": "2025-12-24T09:51:27.47378+03:00",
  "signedAt": null,
  "signedBy": null,
  "signature": null
}
```

---

### 3. Получить контракт
```http
GET /api/contracts/{contractId}
```

**Описание:** Возвращает детальную информацию о контракте.

**Path Parameters:**
- `contractId` (integer) - ID контракта

**Response:** Аналогичен ответу создания контракта.

---

### 4. Сохранить изменения контракта
```http
PUT /api/contracts/{contractId}
```

**Описание:** Обновляет контракт. Можно изменить условия (только в DRAFT) и статус (DRAFT → READY).

**Path Parameters:**
- `contractId` (integer) - ID контракта

**Request Body:**
```json
{
  "termsId": 1,
  "status": "READY"
}
```

**Примечания:**
- `termsId` можно изменить только когда контракт в статусе DRAFT
- `status` может быть только "DRAFT" или "READY"
- Нельзя редактировать контракты со статусом SIGNED или REVOKED

**Response:** Обновленный контракт.

---

### 5. Подписать контракт
```http
POST /api/contracts/{contractId}/sign
```

**Описание:** Подписывает контракт пациентом. Контракт должен быть в статусе READY.

**Path Parameters:**
- `contractId` (integer) - ID контракта

**Request Body:**
```json
{
  "patientId": 7,
  "signedBy": "Иван Иванов",
  "signature": "e-signature-hash-12345"
}
```

**Response:**
```json
{
  "contractId": 3,
  "patientId": 7,
  "termsId": 1,
  "termsVersion": 1,
  "termsTitle": "Base Medical Service Contract v1",
  "termsSnapshot": "This contract defines...",
  "status": "SIGNED",
  "createdAt": "2025-12-24T06:51:27.473766Z",
  "updatedAt": "2025-12-24T09:51:52.344966+03:00",
  "signedAt": "2025-12-24T09:51:52.344957+03:00",
  "signedBy": "Иван Иванов",
  "signature": "e-signature-hash-12345"
}
```

**Правила:**
- Контракт должен быть в статусе READY
- patientId в запросе должен совпадать с patientId контракта
- После подписания статус меняется на SIGNED, контракт больше нельзя редактировать

---

### 6. Получить все контракты пациента
```http
GET /api/contracts/patient/{patientId}
```

**Описание:** Возвращает список всех контрактов конкретного пациента.

**Path Parameters:**
- `patientId` (integer) - ID пациента

**Response:**
```json
[
  {
    "contractId": 3,
    "patientId": 7,
    "status": "SIGNED",
    ...
  },
  {
    "contractId": 4,
    "patientId": 7,
    "status": "READY",
    ...
  }
]
```

---

## Статусы контракта

| Статус | Описание |
|--------|----------|
| DRAFT | Черновик, можно редактировать условия и статус |
| READY | Готов к подписанию, редактировать условия нельзя |
| SIGNED | Подписан, нельзя редактировать |
| REVOKED | Отозван, нельзя редактировать |

## Workflow

```
1. Создание контракта → DRAFT
2. Редактирование (опционально) → DRAFT
3. Сохранение как готовый → READY
4. Подпись пациента → SIGNED
```

## Обработка ошибок

### 400 Bad Request
- Неверный формат данных
- Попытка установить недопустимый статус

### 404 Not Found
- Пациент не найден
- Контракт не найден
- Условия (terms) не найдены

### 500 Internal Server Error
- "Contract belongs to another patient" - попытка подписать чужой контракт
- "Contract must be READY to sign" - попытка подписать контракт не в статусе READY
- "Contract is not editable in status: X" - попытка редактировать неизменяемый контракт
- "Terms can be changed only in DRAFT status" - попытка изменить условия не в DRAFT

## Примеры использования

### Пример 1: Полный цикл создания и подписания контракта

```bash
# 1. Получить доступные условия
curl http://localhost:8080/api/contracts/terms

# 2. Создать контракт
curl -X POST http://localhost:8080/api/contracts \
  -H "Content-Type: application/json" \
  -d '{"patientId": 7, "termsId": 1}'

# 3. Изменить статус на READY
curl -X PUT http://localhost:8080/api/contracts/3 \
  -H "Content-Type: application/json" \
  -d '{"status": "READY"}'

# 4. Подписать контракт
curl -X POST http://localhost:8080/api/contracts/3/sign \
  -H "Content-Type: application/json" \
  -d '{"patientId": 7, "signedBy": "Иван Иванов", "signature": "e-sig-hash"}'
```

### Пример 2: Изменение условий в DRAFT

```bash
# Создать контракт
curl -X POST http://localhost:8080/api/contracts \
  -H "Content-Type: application/json" \
  -d '{"patientId": 7, "termsId": 1}'

# Изменить условия (только в DRAFT)
curl -X PUT http://localhost:8080/api/contracts/5 \
  -H "Content-Type: application/json" \
  -d '{"termsId": 2}'

# Перевести в READY
curl -X PUT http://localhost:8080/api/contracts/5 \
  -H "Content-Type: application/json" \
  -d '{"status": "READY"}'
```

## База данных

### Таблица: terms_and_conditions
Хранит шаблоны условий договора.

| Поле | Тип | Описание |
|------|-----|----------|
| terms_id | integer | PK |
| title | varchar | Название |
| content | text | Текст условий |
| version | integer | Версия (уникальная) |
| is_active | boolean | Активность |
| created_at | timestamptz | Дата создания |

### Таблица: contracts
Хранит контракты пациентов.

| Поле | Тип | Описание |
|------|-----|----------|
| contract_id | integer | PK |
| patient_id | integer | FK → patients |
| terms_id | integer | FK → terms_and_conditions |
| terms_snapshot | text | Снимок условий на момент создания |
| status | varchar | DRAFT / READY / SIGNED / REVOKED |
| created_at | timestamptz | Дата создания |
| updated_at | timestamptz | Дата обновления |
| signed_at | timestamptz | Дата подписания |
| signed_by | varchar | Кем подписано |
| signature | varchar | Электронная подпись |

## Важные замечания

1. **terms_snapshot** сохраняет текст условий на момент создания контракта. Даже если исходные условия (terms_and_conditions) изменятся, контракт сохранит свою версию.

2. **Безопасность**: Endpoint `/sign` проверяет, что patientId в запросе совпадает с владельцем контракта.

3. **Неизменяемость**: Контракты в статусе SIGNED или REVOKED нельзя изменить.

4. **Версионирование**: Каждый контракт хранит ссылку на версию условий (termsVersion).

## Что было реализовано

✅ Все 5 основных endpoints согласно требованиям  
✅ Дополнительный endpoint для получения контрактов пациента  
✅ Валидация и обработка ошибок  
✅ Сохранение снимка условий (terms_snapshot)  
✅ Полный lifecycle контрактов (DRAFT → READY → SIGNED)  
✅ База данных с индексами и foreign keys  
✅ Тестовые данные для terms_and_conditions  
✅ Все endpoints протестированы и работают корректно

