# ❓ 문제: 사용자의 즉각적인 응답 기대

- 사용자는 환전 요청을 제출했을 때, 자신의 요청이 즉시 처리되었는지에 대한 피드백을 원함
- 요청을 보낸 후 한참 뒤에야 응답을 받거나, 처리 상태를 별도로 확인해야 한다면 사용자 경험은 저하될 것
  - 이는 금융 서비스의 신뢰도와 직결됨

<br>

# ❗ 해결: HTTP 기반 동기 처리 구현

- FxBank 환전 서비스는 사용자 기대를 충족시키기 위해 환전 요청의 핵심 단계를 HTTP 기반의 동기 통신으로 구현

<br>

## 1. 구현

### 1.1. ExchangeCurrencyService

- `exchangeCurrency()`: `exchangeCurrencySagaOrchestrator.startExchange()`를 호출해 환전 로직 시작
  - 내부적으로는 Saga 패턴을 통해 여러 마이크로서비스 간의 비동기적인 협업이 이루어질 수도
  - 하지만 사용자에게는 `exchangeCurrency` 호출이 완료되는 시점에 HTTP 응답이 즉시 반환됨

```java
@UseCase
@RequiredArgsConstructor
public class ExchangeCurrencyService implments ExchangeCurrencyUseCase {

    private final ExchangeCurrencySagaOrchestrator exchangeCurrencySagaOrchestrator;
    
    @Override
	public ExchangeDetail exchangeCurrency(ExchangeCurrencyCommand command) {
    	var toBeRequested = toModel(command);
    	return exchangeCurrencySagaOrchestrator.startExchange(toBeRequested); // Saga 시작 및 결과 반환
	}
    
    // ... (생략) ...
}

```

<br>

### 1.2. ExchangeCurrencySagaOrchestratorImpl

- `startExchange()`: 원화 출금(`withdrawService.withdraw()`)과 외화 입금(`depositService.deposit()`)을 순차적으로 호출

```java
@SagaOrchestrator
@RequiredArgsConstructor
public class ExchangeCurrencySagaOrchestratorImpl implements ExchangeCurrencySagaOrchestrator {

    private final CreateExchangeRequestService createExchangeRequestService;
	private final WithdrawService withdrawService;
	private final DepositService depositService;
	private final CompleteExchangeRequestService completeExchangeRequestService;
    
    // ... (생략) ...

    @Override
    public ExchangeDetail startExchange(ExchangeRequest toBeRequested) {
        var requested = createExchangeRequestService.createExchangeRequest(toBeRequested);
        var withdrawn = withdrawService.withdraw(requested); // 1. 원화 출금
        var deposited = depositService.deposit(requested);   // 2. 외화 입금
        var exchanged = completeExchangeRequestService.succeedExchange(requested);
        return new ExchangeDetail(withdrawn, deposited, exchanged);
    }
    
    // ... (생략) ...
}
```

<br>

### 1.3. WebClientConfig

- `createWebClient()`: 타임아웃 설정

```java
@Slf4j
@Configuration
public class WebClientConfig {

    private static final int CONNECT_TIMEOUT_MILLIS = 2000;
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration WRITE_TIMEOUT = Duration.ofSeconds(2);

    // ... (생략) ...
    
    private WebClient createWebClient(String baseUrl, CircuitBreaker circuitBreaker) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MILLIS)
                .responseTimeout(READ_TIMEOUT)
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(READ_TIMEOUT.toSeconds(), TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(WRITE_TIMEOUT.toSeconds(), TimeUnit.SECONDS)));
        
	    // ... (생략) ...
    }
}
```

<br>

## 2. 기술 선택의 이유 및 장점

### 2.1. 즉각적인 사용자 피드백 

*   사용자에게 환전 요청에 대한 즉각적인 성공/실패 여부를 알려줄 수 있음
    *   사용자의 불안감을 해소하고 서비스에 대한 신뢰도를 높임

<br>

### 2.2. 간단한 클라이언트 구현

*   클라이언트 측에서는 HTTP 요청-응답 모델에 따라 결과를 처리하면 되므로, 구현이 비교적 간단
    *   별도의 폴링 로직이나 웹훅 수신 로직을 구현할 필요가 없음

<br>

### 2.3. 직관적인 흐름 

*   개발자 관점에서도 요청부터 응답까지의 흐름이 직관적이어서 디버깅 및 이해가 용이

<br>

## 3. 단점 및 트레이드오프

### 3.1. 블로킹(Blocking) 특성 

*   동기 호출은 백엔드에서 요청 처리가 완료될 때까지 클라이언트가 대기해야
*   만약 백엔드 처리 시간이 길어진다면 클라이언트가 응답을 받기까지 지연이 발생해 사용자 경험에 부정적인 영향을 줄 수도

<br>

### 3.2. 타임아웃 위험

*   백엔드 처리 시간이 너무 길어지면 클라이언트 또는 중간 프록시에서 타임아웃이 발생할 수도

<br>

### 3.3. 서비스 간 결합도

*   사용자에게 즉각적인 응답을 주기 위해 환전 서비스는 계좌 서비스와 같은 외부 서비스와의 통신을 동기적으로 수행해야
*   결국 서비스 간의 일시적인 결합도가 높아져, 외부 서비스의 장애가 환전 서비스에 직접적인 영향을 미칠 수 있는 위험을 내포

<br>

## 4. 다른 기술과의 비교 및 적합성

### 4.1. 100% 비동기 접근 방식

- 요청을 큐에 넣고 즉시 202 Accepted 응답을 반환한 뒤, 실제 처리 결과는 웹훅이나 별도의 알림 채널로 통보하는 것도 가능
- 하지만 이 경우 요청이 실제로 어떻게 진행되고 있는지 알리기 위해 추가적인 노력을 기울여야
  - 별도의 폴링 로직이나 웹훅 수신 로직을 구현해야

<br>

### 4.2. 동기 + 비동기 접근 방식

- 핵심적인 출금 및 입금 요청을 동기적으로 처리해, 사용자가 기다리는 동안의 불확실성을 최소화하고 즉각적인 피드백을 제공
- 내부적으로는 Saga 패턴을 통해 분산 환경에서의 데이터 정합성을 비동기적으로 보장
  - 사용자에게 노출되는 최전선에서는 동기적인 응답을 통해 최적의 사용자 경험을 제공하는 트레이드오프를 선택한 것

<br>

# ❓ 문제: 동기 처리 시 외부 시스템 장애 전파

- 환전 서비스가 요청을 보내는 계좌 서비스 중 하나가 네트워크 문제, 과부하, 또는 내부 오류로 인해 응답이 지연되거나 실패할 가능성 존재
- 계좌 서비스의 상황을 모르는 환전 서비스는 응답을 기다리느라 리소스가 고갈될 수도
- 결국 환전 서비스 자체의 성능 저하를 넘어, 다른 정상적인 요청 처리까지 마비시키는 장애 전파로 이어짐

<br>

# ❗ 해결: Resilience4j 서킷 브레이커 도입

## 0. Resilience4j란?

- Resilience4j는 외부 서비스 호출의 실패를 감지하고, 일정 임계치를 넘어서면 해당 서비스로의 호출을 일시적으로 차단
  - 전기 회로의 차단기처럼, 문제가 발생한 회로를 끊어 전체 시스템을 보호하는 서킷 브레이커 패턴을 구현

<br>

## 1. 구현

### 1.1. application.yml

- 서킷 브레이커를 다음과 같이 정의해, 외화 계좌 서비스와 원화 계좌 서비스에 대한 호출에 적용

  ```yaml
  resilience4j:
    circuitbreaker:
      instances:
        fxCircuitBreaker:
          sliding-window-size: 10
          minimum-number-of-calls: 5
          failure-rate-threshold: 50
          wait-duration-in-open-state: 10s
          permitted-number-of-calls-in-half-open-state: 3
          record-exceptions:
            - org.springframework.web.reactive.function.client.WebClientResponseException
            - org.springframework.web.reactive.function.client.WebClientRequestException
            - java.util.concurrent.TimeoutException
        krwCircuitBreaker:
        	# 이하 동일
  ```

  *   `sliding-window-size`: 최근 10개의 호출 결과를 기준으로 실패율을 계산
  *   `minimum-number-of-calls`: 최소 5개 이상의 호출이 발생해야 서킷 브레이커가 실패율을 계산하기 시작
  *   `failure-rate-threshold`: 실패율이 50%를 초과하면 서킷을 `OPEN` 상태로 전환
  *   `wait-duration-in-open-state`: `OPEN` 상태의 서킷을 10초 동안 유지하며 모든 호출을 즉시 실패 처리
  *   `record-exceptions`: 특정 예외 발생 시 이를 실패로 기록

  *   `permitted-number-of-calls-in-half-open-state`: `OPEN` 상태의 서킷을 10초 후 `HALF_OPEN` 상태로 전환하고, 3개의 호출만 허용
      *   3개의 호출이 성공하면 서킷을 `CLOSED` 상태로 전환
      *   3개의 호출이 실패하면 다시 `OPEN` 상태로 전환


<br>

### 1.2. WebClientConfig

- `createWebClient()`: CircuitBreaker 설정

```java
@Slf4j
@Configuration
public class WebClientConfig {

    private final CircuitBreaker fxCircuitBreaker;
    private final CircuitBreaker krwCircuitBreaker;

    // ... (생략) ...
    
    private WebClient createWebClient(String baseUrl, CircuitBreaker circuitBreaker) {
	    // ... (생략) ...
        return WebClient.builder()
                .filter(circuitBreakerFilter(circuitBreaker))
            	// ... (생략) ...
	            .build();
    }
    
    private ExchangeFilterFunction circuitBreakerFilter(CircuitBreaker circuitBreaker) {
        return (request, next) -> next.exchange(request)
                .transform(CircuitBreakerOperator.of(circuitBreaker));
    }
    // ... (생략) ...
}
```

<br>

## 2. 기술 선택의 이유 및 장점

### 2.1. 장애 전파 방지 

*   외부 서비스의 장애가 환전 서비스로 전파되는 것을 효과적으로 차단하여, 환전 서비스 자체의 안정적인 운영을 보장

<br>

### 2.2. 시스템 복원력 향상 

*   문제가 발생한 서비스에 대한 호출을 일시적으로 중단해, 해당 서비스가 복구될 시간을 벌어줌
*   복구 후에는 자동으로 재시도를 허용해, 시스템의 전반적인 복원력을 높임

<br>

### 2.3. 리소스 보호 

*   실패하는 호출에 대한 불필요한 리소스(스레드, 네트워크 연결 등) 낭비를 막아 환전 서비스의 리소스를 보호

<br>

### 2.4. 경량 라이브러리 

*   Resilience4j는 Netflix Hystrix의 후속으로, 더 가볍고 함수형 프로그래밍 패러다임을 지향하여 오버헤드가 적음

<br>

## 3. 단점 및 트레이드오프

### 3.1. 복잡성 증가 

*   적절한 임계치 설정과 폴백(Fallback) 로직 구현이 필요하며, 모니터링 시스템과의 연동도 중요

<br>

### 3.2. 일시적인 기능 저하 

*   서킷 브레이커가 `OPEN` 상태가 되면 해당 외부 서비스와의 통신이 차단되므로, 해당 기능(예: 계좌 입출금)은 일시적으로 사용할 수 없게 됨
*   따라서 중요한 비즈니스 로직의 경우 폴백 로직을 통해 사용자에게 메시지나 대체 기능을 제공해야

<br>

### 3.3. 설정의 중요성 

*   파라미터(`sliding-window-size`, `failure-rate-threshold` 등 ) 설정이 서비스의 특성과 부하 패턴에 맞지 않으면 오히려 오작동하거나 효과를 보지 못할 수도

<br>

## 4. 다른 기술과의 비교 및 적합성

### 4.1. Hystrix vs Resilience4j 

*   과거에는 Hystrix가 널리 사용되었으나, 현재는 유지보수가 중단됨
*   Resilience4j가 Hystrix의 대안으로 더 가볍고, Java 8의 함수형 프로그래밍을 적극 활용하며, Micrometer와 같은 최신 모니터링 도구와의 통합이 용이

<br>

### 4.2. 타임아웃 vs 서킷 브레이커 

*   타임아웃은 개별 호출의 지연은 방지하지만, 반복적인 실패 호출로 인한 리소스 고갈은 막지 못함
*   서킷 브레이커는 타임아웃을 포함한 다양한 실패를 감지하고, 시스템 전체를 보호하는 더 상위 개념의 장애 내성 패턴