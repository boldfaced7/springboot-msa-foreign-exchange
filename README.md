# Spring Boot MSA 기반 환전 서비스

**MSA 환경에서 분산 트랜잭션의 데이터 정합성 문제를 `Orchestration Saga` 패턴으로 해결한 환전 서비스입니다.** 이 프로젝트는 마이크로서비스의 장점을 살리면서도, 분산 환경이 가진 복잡성과 데이터 불일치 문제를 어떻게 해결했는지에 대한 구체적인 고민과 구현을 담았습니다.

<br>

## 🎯 해결 과제: 분산 트랜잭션

MSA 환경에서는 여러 서비스에 걸친 작업을 하나의 DB 트랜잭션으로 묶을 수 없습니다. 예를 들어, 사용자의 원화(KRW) 계좌에서 출금은 성공했지만 외화(FX) 계좌 입금이 실패하면, 데이터가 틀어져 사용자 자산에 문제가 생깁니다. 이 프로젝트는 **어떤 장애 상황에서도 데이터 정합성을 유지하는 것**을 핵심 목표로 삼았습니다.

<br>

## 💡 핵심 전략: Orchestration Saga Pattern

분산 트랜잭션을 관리하기 위해 **오케스트레이션(Orchestration) 기반의 사가 패턴**을 선택했습니다. 중앙 관리자인 '오케스트레이터'가 전체 흐름을 지휘하는 방식이, 여러 서비스가 이벤트를 주고받으며 진행되는 '코레오그래피(Choreography)' 방식보다 복잡한 환전 시나리오와 다양한 예외 상황을 통제하기에 더 낫다고 판단했습니다.

- **Exchange Service (Orchestrator):** 환전 프로세스 전체를 지휘합니다. 각 서비스에 로컬 트랜잭션을 순차적으로 요청하고, 실패 시에는 **보상 트랜잭션**을 실행시켜 이전 작업을 되돌립니다.

<br>

## 🏛️ 시스템 아키텍처
![System Architecture.png](docs/assets/System%20Architecture.png)

### 마이크로서비스 구성

| Service                 | Role           | Description                                                    |
|:------------------------|:---------------|:---------------------------------------------------------------|
| **Exchange Service**    | `Orchestrator` | 환전 사가 트랜잭션의 전체 흐름을 조율하고, 각 참여 서비스의 트랜잭션과 보상 트랜잭션을 관리합니다.       |
| **KRW Account Service** | `Participant`  | 원화(KRW) 계좌의 입출금, 출금 취소(보상) 등 로컬 트랜잭션을 담당합니다.                   |
| **FX Account Service**  | `Participant`  | 외화(FX) 계좌의 입출금, 출금 취소(보상) 등 로컬 트랜잭션을 담당합니다.                    |
| **Message Scheduler**   | `Helper`       | 외부 서비스의 일시적 장애 시, 재시도 메시지를 Kafka에 예약 발행하여 최종적인 데이터 정합성을 보장합니다. |

<br>

## ✨ 주요 기술 결정 및 상세 구현

### 1. UX와 안정성을 고려한 하이브리드 통신

- **문제:** 사용자에게 빠른 피드백을 주면서도, 서비스 장애가 다른 서비스로 전파되는 것을 막아야 했습니다.
- **해결:**
    - **동기 통신 (Sync, REST API):** 사용자가 즉각적인 결과를 원하는 핵심 흐름(출금→입금)은 동기 방식으로 처리해 빠른 응답을 보장했습니다.
    - **비동기 통신 (Async, Kafka):** 실패 시의 롤백(보상 트랜잭션)처럼 즉각적인 처리가 중요하지 않은 작업은 비동기 메시지로 처리했습니다. 이를 통해 서비스 간 의존성을 낮춰 한 서비스의 장애가 전체로 퍼지는 것을 막았습니다.

### 2. 네트워크 불확실성 대응: 재조회와 메시지 스케줄러

- **문제:** 타임아웃 등으로 외부 서비스의 응답을 받지 못하면, 요청의 성공 여부를 알 수 없는 **'Unknown' 상태**가 되어 데이터가 틀어질 수 있습니다.
- **해결:** 2단계에 걸쳐 이 문제를 해결했습니다.
    - **1단계 (상태 재조회):** 오케스트레이터가 해당 서비스에 트랜잭션의 최종 결과를 다시 물어봐 상태를 명확히 합니다.
    - **2단계 (지연 재시도):** 재조회마저 실패하면, 재시도 처리를 외부 **메시지 스케줄러**에 위임합니다. 스케줄러는 Kafka에 지연 메시지를 보내고, 일정 시간 뒤 다시 시도하여 외부 서비스가 복구될 시간을 벌어주며 **최종적 일관성(Eventual Consistency)** 을 보장합니다.

### 3. 중복 요청 방지를 위한 2단계 멱등성 보장

- **문제:** 네트워크 오류나 메시지 큐의 특성으로 같은 요청이 여러 번 들어와 중복 출금이 발생할 수 있습니다.
- **해결:** 2단계에 걸쳐 중복 요청을 막았습니다.
    - **1단계 (Redis 활용):** Spring AOP와 Redis의 `setIfAbsent`를 이용해 요청마다 고유한 키를 캐싱했습니다. 비즈니스 로직 실행 전에 중복 요청을 먼저 걸러내 불필요한 DB 접근과 시스템 부하를 줄였습니다.
    - **2단계 (DB Unique Key 활용):** Redis에 문제가 생기는 최악의 경우를 대비해, DB 테이블에 `UNIQUE KEY` 제약 조건을 걸어 데이터베이스 단에서 중복 저장을 원천적으로 막는 최종 방어선을 구축했습니다.

### 4. 비관적 락(Pessimistic Lock)으로 동시성 문제 해결

- **문제:** 여러 비동기 이벤트가 동시에 같은 데이터(환전 요청 상태)를 수정하려 할 때 충돌이 발생할 수 있습니다.
- **해결:** JPA의 **`@Lock(LockModeType.PESSIMISTIC_WRITE)`**을 사용해 DB 단에서 **비관적 락**을 걸었습니다.
    - **선택 이유:** 데이터 정합성이 최우선인 금융 거래에서는, 일단 수정을 시도하고 충돌 시 재처리하는 '낙관적 락'보다, 처음부터 데이터 접근을 독점하여 동시 수정을 원천적으로 막는 '비관적 락'이 더 안전하고 확실한 방법이라 판단했습니다.

### 5. 시스템 신뢰도 확보를 위한 주요 설계

- **장애 격리:** `Resilience4j`의 **서킷 브레이커**를 적용해 특정 서비스의 장애가 시스템 전체로 퍼지는 것을 막았습니다.
- **추적성 확보:** 사가의 모든 상태 변경 이력을 `@Async`와 `@TransactionalEventListener`를 통해 비동기적으로 DB에 기록하여, 장애 발생 시 문제의 원인을 빠르게 추적할 수 있도록 했습니다.
- **메시지 처리 보장:** Kafka 프로듀서의 `멱등성 옵션`과 컨슈머의 `수동 커밋`, `Dead Letter Topic(DLT)` 패턴을 적용해, 메시지가 중복되거나 유실되지 않도록 보장했습니다.

### 6. 운영 환경 수준의 통합 테스트 전략

- **문제:** 단위 테스트만으로는 MSA의 복잡한 상호작용과 비동기 흐름을 완벽히 검증하기 어렵습니다.
- **해결:** 실제 운영 환경과 거의 동일한 테스트 환경을 코드로 구축하여 검증의 신뢰도를 높였습니다.
    - **`Testcontainers`:** MySQL, Kafka, Redis 등 필요한 인프라를 테스트 시점에 도커 컨테이너로 직접 띄워 사용해, 환경에 따른 테스트 결과의 변수를 없앴습니다.
    - **`MockWebServer`:** 외부 계좌 서비스를 모킹하여, 타임아웃이나 서버 오류 등 예측 가능한 모든 예외 상황을 시뮬레이션하고 그에 대한 우리 시스템의 반응을 정밀하게 검증했습니다.

<br>

## 🏁 Tech Stacks

- **Backend:** Java 17, Spring Boot 3, Spring Data JPA
- **Architecture:** MSA, Hexagonal Architecture, DDD, EDA, Saga Pattern (Orchestration)
- **Database:** MySQL, Redis(for Idempotency Key Caching)
- **Messaging:** Kafka
- **Resilience:** Resilience4j(Circuit Breaker)
- **Testing:** JUnit 5, Mockito, Testcontainers, MockWebServer
- **DevOps:** Docker, Gradle

<br>

## 📚 Project Documentation

프로젝트를 진행하며 학습하고 고민했던 내용들을 아래 기술 블로그 포스트로 상세히 정리했습니다.

*   [API 명세서](docs/API_SPEC.md)
*   [환전 프로세스 플로우차트](docs/FLOWCHART.md)
*   [주요 흐름 시퀀스 다이어그램](docs/SEQUENCE_DIAGRAMS.md)


* **기술 구현 포스트**
  1. [사용자 경험(UX) 최적화를 위한 동기 처리 전략](https://github.com/boldfaced7/springboot-msa-foreign-exchange/blob/main/docs/posts/1%20-%20%EC%82%AC%EC%9A%A9%EC%9E%90%20%EA%B2%BD%ED%97%98(UX)%20%EC%B5%9C%EC%A0%81%ED%99%94%EB%A5%BC%20%EC%9C%84%ED%95%9C%20%EB%8F%99%EA%B8%B0%20%EC%B2%98%EB%A6%AC%20%EC%A0%84%EB%9E%B5.md)
  2. [오케스트레이션 사가 패턴과 보상 트랜잭션](https://github.com/boldfaced7/springboot-msa-foreign-exchange/blob/main/docs/posts/2%20-%20%EC%98%A4%EC%BC%80%EC%8A%A4%ED%8A%B8%EB%A0%88%EC%9D%B4%EC%85%98%20%EC%82%AC%EA%B0%80%20%ED%8C%A8%ED%84%B4%EA%B3%BC%20%EB%B3%B4%EC%83%81%20%ED%8A%B8%EB%9E%9C%EC%9E%AD%EC%85%98.md)
  3. [Kafka 기반 비동기 통신으로 서비스 간 결합도 감소와 장애 전파 최소화](https://github.com/boldfaced7/springboot-msa-foreign-exchange/blob/main/docs/posts/3%20-%20Kafka%20%EA%B8%B0%EB%B0%98%20%EB%B9%84%EB%8F%99%EA%B8%B0%20%ED%86%B5%EC%8B%A0%EC%9C%BC%EB%A1%9C%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EA%B0%84%20%EA%B2%B0%ED%95%A9%EB%8F%84%20%EA%B0%90%EC%86%8C%EC%99%80%20%EC%9E%A5%EC%95%A0%20%EC%A0%84%ED%8C%8C%20%EC%B5%9C%EC%86%8C%ED%99%94.md)
  4. [비정상적 환전 실패 시 데이터 정합성 확보](https://github.com/boldfaced7/springboot-msa-foreign-exchange/blob/main/docs/posts/4%20-%20%EB%B9%84%EC%A0%95%EC%83%81%EC%A0%81%20%ED%99%98%EC%A0%84%20%EC%8B%A4%ED%8C%A8%20%EC%8B%9C%20%EB%8D%B0%EC%9D%B4%ED%84%B0%20%EC%A0%95%ED%95%A9%EC%84%B1%20%ED%99%95%EB%B3%B4.md)
  5. [멱등성 확보로 중복 입출금 처리 차단](https://github.com/boldfaced7/springboot-msa-foreign-exchange/blob/main/docs/posts/5%20-%20%EB%A9%B1%EB%93%B1%EC%84%B1%20%ED%99%95%EB%B3%B4%EB%A1%9C%20%EC%A4%91%EB%B3%B5%20%EC%9E%85%EC%B6%9C%EA%B8%88%20%EC%B2%98%EB%A6%AC%20%EC%B0%A8%EB%8B%A8.md)
  6. [동시성 문제 해결로 객체 수정 충돌 방지](https://github.com/boldfaced7/springboot-msa-foreign-exchange/blob/main/docs/posts/6%20-%20%EB%8F%99%EC%8B%9C%EC%84%B1%20%EB%AC%B8%EC%A0%9C%20%ED%95%B4%EA%B2%B0%EB%A1%9C%20%EA%B0%9D%EC%B2%B4%20%EC%88%98%EC%A0%95%20%EC%B6%A9%EB%8F%8C%20%EB%B0%A9%EC%A7%80.md)
  7. [모든 환전 시나리오를 테스트해 서비스의 신뢰도 확보](https://github.com/boldfaced7/springboot-msa-foreign-exchange/blob/main/docs/posts/7%20-%20%EB%AA%A8%EB%93%A0%20%ED%99%98%EC%A0%84%20%EC%8B%9C%EB%82%98%EB%A6%AC%EC%98%A4%EB%A5%BC%20%ED%85%8C%EC%8A%A4%ED%8A%B8%ED%95%B4%20%EC%84%9C%EB%B9%84%EC%8A%A4%EC%9D%98%20%EC%8B%A0%EB%A2%B0%EB%8F%84%20%ED%99%95%EB%B3%B4.md)
  8. [트랜잭션 상태 경로 기록으로 장애 추적성 확보](https://github.com/boldfaced7/springboot-msa-foreign-exchange/blob/main/docs/posts/8%20-%20%ED%8A%B8%EB%9E%9C%EC%9E%AD%EC%85%98%20%EC%83%81%ED%83%9C%20%EA%B2%BD%EB%A1%9C%20%EA%B8%B0%EB%A1%9D%EC%9C%BC%EB%A1%9C%20%EC%9E%A5%EC%95%A0%20%EC%B6%94%EC%A0%81%EC%84%B1%20%ED%99%95%EB%B3%B4.md)