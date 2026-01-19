# Contracts API - Quick Reference для Frontend

**Base URL:** `http://localhost:8080/api/contracts`

**CORS:** Настроен для `http://localhost:3000`

---

## 📋 Endpoints

### 1. Получить все Terms & Conditions

```http
GET /api/contracts/terms
```

**Response:**
```json
[
  {
    "termsId": 1,
    "title": "Base Medical Service Contract v1",
    "content": "This contract defines...",
    "version": 1,
    "isActive": true,
    "createdAt": "2025-12-22T06:22:30.273759Z"
  }
]
```

---

### 2. Получить все контракты пациента

```http
GET /api/contracts/patient/{patientId}
```

**Path params:**
- `patientId` (number) - ID пациента

**Response:**
```json
[
  {
    "contractId": 3,
    "patientId": 7,
    "termsId": 1,
    "termsVersion": 1,
    "termsTitle": "Base Medical Service Contract v1",
    "termsSnapshot": "текст условий...",
    "status": "SIGNED",
    "createdAt": "2025-12-24T06:51:27.473766Z",
    "updatedAt": "2025-12-24T06:51:52.344966Z",
    "signedAt": "2025-12-24T06:51:52.344957Z",
    "signedBy": "Иван Иванов",
    "signature": "e-signature-hash-12345"
  }
]
```

---

### 3. Создать контракт

```http
POST /api/contracts
```

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
  "termsSnapshot": "текст условий...",
  "status": "DRAFT",
  "createdAt": "2025-12-24T09:51:27.473766+03:00",
  "updatedAt": "2025-12-24T09:51:27.47378+03:00",
  "signedAt": null,
  "signedBy": null,
  "signature": null
}
```

---

### 4. Получить контракт

```http
GET /api/contracts/{contractId}
```

**Path params:**
- `contractId` (number) - ID контракта

**Response:** Аналогичен ответу создания контракта

---

### 5. Обновить контракт

```http
PUT /api/contracts/{contractId}
```

**Path params:**
- `contractId` (number) - ID контракта

**Request Body:**
```json
{
  "termsId": 1,      // Опционально, только в DRAFT
  "status": "READY"  // Опционально: "DRAFT" или "READY"
}
```

**Response:** Обновленный контракт

**Правила:**
- `termsId` можно изменить только в статусе DRAFT
- `status` может быть только "DRAFT" или "READY"
- Нельзя редактировать контракты в статусе SIGNED или REVOKED

---

### 6. Подписать контракт

```http
POST /api/contracts/{contractId}/sign
```

**Path params:**
- `contractId` (number) - ID контракта

**Request Body:**
```json
{
  "patientId": 7,
  "signedBy": "Иван Иванов",
  "signature": "e-signature-hash-12345"
}
```

**Response:** Контракт со статусом SIGNED

**Правила:**
- Контракт должен быть в статусе READY
- patientId должен совпадать с владельцем контракта

---

## 📊 Статусы контракта

| Статус | Описание | Редактируемый |
|--------|----------|---------------|
| `DRAFT` | Черновик | ✅ Да |
| `READY` | Готов к подписанию | ❌ Нет (только подпись) |
| `SIGNED` | Подписан | ❌ Нет |
| `REVOKED` | Отозван | ❌ Нет |

---

## 🔄 Типичный Workflow

```javascript
// 1. Загрузить список условий для выбора
const terms = await getTerms();

// 2. Создать контракт (статус DRAFT)
const contract = await createContract(patientId, termsId);

// 3. (Опционально) Изменить условия или статус
await updateContract(contract.contractId, { status: "READY" });

// 4. Подписать контракт
await signContract(contract.contractId, {
  patientId,
  signedBy: "Имя пациента",
  signature: "hash"
});

// 5. Получить все контракты пациента
const allContracts = await getPatientContracts(patientId);
```

---

## ⚠️ Обработка ошибок

### 404 Not Found
```json
"Patient not found: 7"
"Contract not found: 123"
"Terms not found: 5"
```

### 500 Internal Server Error
```json
"Contract belongs to another patient"
"Contract must be READY to sign. Current: DRAFT"
"Contract is not editable in status: SIGNED"
"Terms can be changed only in DRAFT status"
```

---

## 💻 Примеры для Frontend (JavaScript/TypeScript)

### Пример сервиса для React/Vue:

```typescript
// contractService.ts
const API_BASE = 'http://localhost:8080/api/contracts';

export const contractAPI = {
  // Получить все условия
  getTerms: async () => {
    const response = await fetch(`${API_BASE}/terms`);
    return response.json();
  },

  // Получить контракты пациента
  getPatientContracts: async (patientId: number) => {
    const response = await fetch(`${API_BASE}/patient/${patientId}`);
    if (!response.ok) throw new Error(await response.text());
    return response.json();
  },

  // Создать контракт
  createContract: async (patientId: number, termsId: number) => {
    const response = await fetch(`${API_BASE}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ patientId, termsId })
    });
    if (!response.ok) throw new Error(await response.text());
    return response.json();
  },

  // Получить контракт
  getContract: async (contractId: number) => {
    const response = await fetch(`${API_BASE}/${contractId}`);
    if (!response.ok) throw new Error(await response.text());
    return response.json();
  },

  // Обновить контракт
  updateContract: async (contractId: number, data: {
    termsId?: number;
    status?: 'DRAFT' | 'READY';
  }) => {
    const response = await fetch(`${API_BASE}/${contractId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
    if (!response.ok) throw new Error(await response.text());
    return response.json();
  },

  // Подписать контракт
  signContract: async (contractId: number, data: {
    patientId: number;
    signedBy: string;
    signature: string;
  }) => {
    const response = await fetch(`${API_BASE}/${contractId}/sign`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
    if (!response.ok) throw new Error(await response.text());
    return response.json();
  }
};
```

### Пример использования в компоненте:

```typescript
// ContractPage.tsx (React)
import { useState, useEffect } from 'react';
import { contractAPI } from './services/contractService';

function ContractPage({ patientId }) {
  const [contracts, setContracts] = useState([]);
  const [terms, setTerms] = useState([]);
  
  useEffect(() => {
    // Загрузить контракты пациента
    contractAPI.getPatientContracts(patientId)
      .then(setContracts)
      .catch(console.error);
    
    // Загрузить доступные условия
    contractAPI.getTerms()
      .then(setTerms)
      .catch(console.error);
  }, [patientId]);
  
  const handleCreateContract = async (termsId) => {
    try {
      const newContract = await contractAPI.createContract(patientId, termsId);
      setContracts([...contracts, newContract]);
    } catch (error) {
      console.error('Ошибка создания контракта:', error);
    }
  };
  
  const handleSignContract = async (contractId) => {
    try {
      const updated = await contractAPI.signContract(contractId, {
        patientId,
        signedBy: "Имя пациента",
        signature: "e-signature-hash"
      });
      setContracts(contracts.map(c => 
        c.contractId === contractId ? updated : c
      ));
    } catch (error) {
      console.error('Ошибка подписания:', error);
    }
  };
  
  return (
    <div>
      <h1>Контракты пациента</h1>
      {contracts.map(contract => (
        <div key={contract.contractId}>
          <h3>{contract.termsTitle}</h3>
          <p>Статус: {contract.status}</p>
          {contract.status === 'READY' && (
            <button onClick={() => handleSignContract(contract.contractId)}>
              Подписать
            </button>
          )}
        </div>
      ))}
    </div>
  );
}
```

---

## 🔧 TypeScript Types

```typescript
export interface TermsAndConditions {
  termsId: number;
  title: string;
  content: string;
  version: number;
  isActive: boolean;
  createdAt: string;
}

export interface Contract {
  contractId: number;
  patientId: number;
  termsId: number;
  termsVersion: number;
  termsTitle: string;
  termsSnapshot: string;
  status: 'DRAFT' | 'READY' | 'SIGNED' | 'REVOKED';
  createdAt: string;
  updatedAt: string;
  signedAt: string | null;
  signedBy: string | null;
  signature: string | null;
}

export interface CreateContractRequest {
  patientId: number;
  termsId: number;
}

export interface UpdateContractRequest {
  termsId?: number;
  status?: 'DRAFT' | 'READY';
}

export interface SignContractRequest {
  patientId: number;
  signedBy: string;
  signature: string;
}
```

---

## 📞 Контакты

Если есть вопросы по API - пишите в чат команды или создайте issue в репозитории.

Backend работает на: `http://localhost:8080`  
Документация: `CONTRACT_API_DOCUMENTATION.md` (подробная версия)

