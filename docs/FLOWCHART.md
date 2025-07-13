# 환전 서비스 플로우차트

## 전체 환전 프로세스 플로우

```mermaid
flowchart TD
    A[환전 요청 시작] --> B[환전 요청 저장]
    B --> C[출금 요청]
    C --> D{출금 결과}
    
    D -- 성공 --> E[출금 성공 저장]
    D -- 실패 --> F[환전 실패]
    D -- 결과 모름 --> G[출금 상태 캐시 저장]
    
    E --> H[입금 요청]
    G --> I[출금 확인 스케줄링]
    I --> J[지연된 출금 확인]
    J --> K{출금 확인 결과}
    
    K -- 성공 --> L[출금 성공 확인]
    K -- 실패 --> M[환전 실패]
    K -- 여전히 모름 --> N{재시도 횟수}
    
    N -- 4회 미만 --> I
    N -- 4회 이상 --> O[경고 메시지 발송]
    O --> P[환전 실패]
    
    L --> Q[출금 취소]
    Q --> P
    
    H --> R{입금 결과}
    
    R -- 성공 --> S[입금 성공 저장]
    R -- 실패 --> T[출금 취소]
    R -- 결과 모름 --> U[입금 상태 캐시 저장]
    
    T --> P
    S --> V[환전 성공]
    
    U --> W[입금 확인 스케줄링]
    W --> X[지연된 입금 확인]
    X --> Y{입금 확인 결과}
    
    Y -- 성공 --> Z[입금 성공 확인]
    Y -- 실패 --> AA[출금 취소]
    Y -- 여전히 모름 --> BB{재시도 횟수}
    
    BB -- 4회 미만 --> W
    BB -- 4회 이상 --> CC[경고 메시지 발송]
    CC --> P
    
    AA --> P
    Z --> V
    
    F --> DD[종료]
    M --> DD
    P --> DD
    V --> DD
```

## 출금 프로세스 상세 플로우

```mermaid
flowchart TD
    A[출금 요청] --> B{출금 API 호출}
    B -- 성공 --> C[출금 성공]
    B -- 실패 --> D[출금 실패]
    B -- 타임아웃/모름 --> E[출금 결과 모름]
    
    C --> F[출금 정보 저장]
    F --> G[출금 완료]
    
    D --> H[출금 실패 처리]
    H --> I[환전 실패]
    
    E --> J[캐시에 상태 저장]
    J --> K[스케줄링된 확인 요청]
    K --> L[지연된 출금 확인]
    
    L --> M{확인 결과}
    M -- 성공 --> N[출금 성공 확인]
    M -- 실패 --> O[출금 실패 확인]
    M -- 여전히 모름 --> P{재시도 횟수}
    
    P -- 4회 미만 --> Q[재스케줄링]
    P -- 4회 이상 --> R[경고 메시지 발송]
    
    Q --> L
    R --> S[환전 실패]
    
    N --> T[출금 취소 필요]
    O --> S
    T --> U[출금 취소 처리]
    U --> S
    
    G --> V[다음 단계: 입금]
    I --> W[종료]
    S --> W
```

## 입금 프로세스 상세 플로우

```mermaid
flowchart TD
    A[입금 요청] --> B{입금 API 호출}
    B -- 성공 --> C[입금 성공]
    B -- 실패 --> D[입금 실패]
    B -- 타임아웃/모름 --> E[입금 결과 모름]
    
    C --> F[입금 정보 저장]
    F --> G[환전 성공]
    
    D --> H[출금 취소]
    H --> I[환전 실패]
    
    E --> J[캐시에 상태 저장]
    J --> K[스케줄링된 확인 요청]
    K --> L[지연된 입금 확인]
    
    L --> M{확인 결과}
    M -- 성공 --> N[입금 성공 확인]
    M -- 실패 --> O[입금 실패 확인]
    M -- 여전히 모름 --> P{재시도 횟수}
    
    P -- 4회 미만 --> Q[재스케줄링]
    P -- 4회 이상 --> R[경고 메시지 발송]
    
    Q --> L
    R --> S[환전 실패]
    
    N --> T[환전 성공]
    O --> U[출금 취소]
    U --> S
    
    G --> V[환전 완료]
    I --> W[종료]
    S --> W
    T --> W
```

## 재시도 정책 플로우

```mermaid
flowchart TD
    A[확인 요청 시작] --> B[재시도 카운터 초기화]
    B --> C[확인 API 호출]
    C --> D{응답 결과}
    
    D -- 성공 --> E[확인 성공]
    D -- 실패 --> F[확인 실패]
    D -- 타임아웃/모름 --> G[확인 결과 모름]
    
    E --> H[성공 처리]
    F --> I[실패 처리]
    
    G --> J{재시도 횟수 확인}
    J -- 최대 미만 --> K[재시도 카운터 증가]
    J -- 최대 도달 --> L[최대 재시도 도달]
    
    K --> M[지연 시간 계산]
    M --> N[스케줄링된 재시도]
    N --> C
    
    L --> O[경고 메시지 발송]
    O --> P[최종 실패 처리]
    
    H --> Q[정상 완료]
    I --> R[실패 완료]
    P --> R
```

## 롤백 처리 플로우

```mermaid
flowchart TD
    A[롤백 시작] --> B{롤백 유형}
    
    B -- 입금 실패 --> C[출금 취소]
    B -- 출금 실패 --> D[환전 실패]
    B -- 시스템 오류 --> E[전체 롤백]
    
    C --> F[출금 취소 API 호출]
    F --> G{취소 결과}
    G -- 성공 --> H[취소 성공]
    G -- 실패 --> I[취소 실패]
    
    I --> J[수동 개입 필요]
    J --> K[관리자 알림]
    
    H --> L[롤백 완료]
    D --> M[환전 실패]
    E --> N[전체 상태 초기화]
    N --> O[롤백 완료]
    
    K --> P[수동 처리 대기]
    L --> Q[종료]
    M --> Q
    O --> Q
    P --> Q
```

## 상태 관리 플로우

```mermaid
flowchart TD
    A[EXCHANGE_CURRENCY_STARTED] --> B[환전 요청 저장]
    B --> C[출금 처리]
    C --> D{출금 결과}
    
    D -- 성공 --> E[WITHDRAWAL_SUCCEEDED]
    D -- 실패 --> F[EXCHANGE_CURRENCY_FAILED]
    D -- 모름 --> G[CHECKING_WITHDRAWAL_REQUIRED]
    
    E --> H[입금 처리]
    G --> I[CHECKING_WITHDRAWAL_REQUIRED]
    I --> J{확인 결과}
    
    J -- 성공 --> K[WITHDRAWAL_SUCCEEDED]
    J -- 실패 --> L[EXCHANGE_CURRENCY_FAILED]
    J -- 모름 --> G
    
    K --> H
    H --> M{입금 결과}
    
    M -- 성공 --> N[EXCHANGE_CURRENCY_SUCCEEDED]
    M -- 실패 --> O[CANCELING_WITHDRAWAL_REQUIRED]
    M -- 모름 --> P[CHECKING_DEPOSIT_REQUIRED]
    
    O --> Q[CANCELING_WITHDRAWAL_REQUIRED]
    P --> R[CHECKING_DEPOSIT_REQUIRED]
    R --> S{확인 결과}
    
    S -- 성공 --> T[EXCHANGE_CURRENCY_SUCCEEDED]
    S -- 실패 --> U[CANCELING_WITHDRAWAL_REQUIRED]
    S -- 모름 --> P
    
    U --> Q
    F --> V[종료]
    L --> V
    N --> W[EXCHANGE_CURRENCY_SUCCEEDED]
    Q --> X[EXCHANGE_CURRENCY_FAILED]
    T --> W
```

## 주요 상태 정의

| 상태 | 설명 | 다음 가능 상태 |
|------|------|----------------|
| `EXCHANGE_CURRENCY_STARTED` | 환전 요청 접수 | `WITHDRAWAL_SUCCEEDED`, `CHECKING_WITHDRAWAL_REQUIRED`, `EXCHANGE_CURRENCY_FAILED` |
| `CHECKING_WITHDRAWAL_REQUIRED` | 출금 처리 중 (결과 모름) | `WITHDRAWAL_SUCCEEDED`, `EXCHANGE_CURRENCY_FAILED` |
| `WITHDRAWAL_SUCCEEDED` | 출금 완료 | `EXCHANGE_CURRENCY_SUCCEEDED`, `CHECKING_DEPOSIT_REQUIRED`, `CANCELING_WITHDRAWAL_REQUIRED` |
| `CHECKING_DEPOSIT_REQUIRED` | 입금 처리 중 (결과 모름) | `EXCHANGE_CURRENCY_SUCCEEDED`, `CANCELING_WITHDRAWAL_REQUIRED` |
| `EXCHANGE_CURRENCY_SUCCEEDED` | 환전 완료 | - |
| `CANCELING_WITHDRAWAL_REQUIRED` | 환전 취소 (롤백) | - |
| `EXCHANGE_CURRENCY_FAILED` | 환전 실패 | - |

## 에러 처리 플로우

```mermaid
flowchart TD
    A[에러 발생] --> B{에러 유형}
    
    B -- 네트워크 오류 --> C[재시도]
    B -- 시스템 오류 --> D[롤백]
    B -- 비즈니스 오류 --> E[실패 처리]
    B -- 타임아웃/모름 --> F[지연 확인]
    
    C --> G{재시도 횟수}
    G -- 최대 미만 --> H[지연 후 재시도]
    G -- 최대 도달 --> I[최대 재시도 초과]
    
    H --> C
    I --> J[경고 메시지]
    J --> K[실패 처리]
    
    D --> L[롤백 실행]
    L --> M[롤백 완료]
    
    E --> N[에러 로깅]
    N --> O[실패 응답]
    
    F --> P[스케줄링]
    P --> Q[지연 확인]
    Q --> R{확인 결과}
    R -- 성공 --> S[정상 처리]
    R -- 실패 --> T[실패 처리]
    R -- 모름 --> F
    
    K --> U[종료]
    M --> U
    O --> U
    S --> V[성공]
    T --> U
```