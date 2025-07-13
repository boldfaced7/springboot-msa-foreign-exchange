# API 명세서 (API SPEC)

## 공통

- Base URL: `/exchange`
- Content-Type: `application/json`
- 인증: (예시) JWT, OAuth2 등 필요시 명시

---

## 1. 외화 매수 (Buy Foreign Currency)

### [POST] `/exchange/buy`

- 외화를 매수(구매)하는 요청을 처리합니다.

#### Request Body

```json
{
  "exchangeId": "string",         // 환전 거래 고유 ID
  "userId": "string",             // 사용자 ID
  "baseCurrency": "USD",          // 기준 통화 (예: USD, KRW 등)
  "baseAmount": 1000,             // 기준 통화 금액
  "quoteAmount": 1300000,         // 상대 통화 금액
  "exchangeRate": 1300.0          // 적용 환율
}
```

#### Response Body

```json
{
  "exchangeId": "string",         // 환전 거래 고유 ID
  "withdrawId": "string",         // 출금 트랜잭션 ID
  "depositId": "string"           // 입금 트랜잭션 ID
}
```

#### 예시

```http
POST /exchange/buy
Content-Type: application/json

{
  "exchangeId": "EX-20240601-0001",
  "userId": "user-123",
  "baseCurrency": "USD",
  "baseAmount": 1000,
  "quoteAmount": 1300000,
  "exchangeRate": 1300.0
}
```

Response:

```json
{
  "exchangeId": "EX-20240601-0001",
  "withdrawId": "WD-20240601-0001",
  "depositId": "DP-20240601-0001"
}
```

---

## 2. 외화 매도 (Sell Foreign Currency)

### [POST] `/exchange/sell`

- 외화를 매도(판매)하는 요청을 처리합니다.

#### Request Body

```json
{
  "exchangeId": "string",         // 환전 거래 고유 ID
  "userId": "string",             // 사용자 ID
  "baseCurrency": "USD",          // 기준 통화 (예: USD, KRW 등)
  "baseAmount": 1000,             // 기준 통화 금액
  "quoteAmount": 1300000,         // 상대 통화 금액
  "exchangeRate": 1300.0          // 적용 환율
}
```

#### Response Body

```json
{
  "exchangeId": "string",         // 환전 거래 고유 ID
  "withdrawId": "string",         // 출금 트랜잭션 ID
  "depositId": "string"           // 입금 트랜잭션 ID
}
```

#### 예시

```http
POST /exchange/sell
Content-Type: application/json

{
  "exchangeId": "EX-20240601-0002",
  "userId": "user-456",
  "baseCurrency": "USD",
  "baseAmount": 500,
  "quoteAmount": 650000,
  "exchangeRate": 1300.0
}
```

Response:

```json
{
  "exchangeId": "EX-20240601-0002",
  "withdrawId": "WD-20240601-0002",
  "depositId": "DP-20240601-0002"
}
```

---

## 3. 공통 에러 응답

```json
{
  "timestamp": "2024-06-01T12:00:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "상세 에러 메시지",
  "path": "/exchange/buy"
}
```