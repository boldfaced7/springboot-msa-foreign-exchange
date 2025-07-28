# ❓ 문제: 보상 트랜잭션의 동기 처리 비효율성 및 위험성

- 환전 서비스는 입금 실패 시 '원화 출금 취소' 보상 트랜잭션을 실행
- 만약 보상 트랜잭션까지 동기적으로 처리한다면 다음과 같은 문제가 발생할 수 있음

<br>

## 1. 불필요한 리소스 점유

- 환전 서비스가 계좌 서비스의 출금 취소 응답을 기다리는 동안 불필요하게 리소스를 점유하게 됨
- 이는 다른 환전 요청 처리에도 영향을 미쳐 전체 시스템의 처리량을 저하시킬 수 있음

<br>

## 2. 강한 의존성

- 환전 서비스가 계좌 서비스에 직접적으로 의존하게 되어, 계좌 서비스의 장애나 지연이 환전 서비스에 직접적인 영향을 미치게 됨

<br>

## 3. 장애 전파 위험

- 보상 트랜잭션 과정에서 계좌 서비스에 문제가 발생할 경우, 그 장애가 환전 서비스로 전파되어 환전 서비스의 안정성을 위협할 수 있음

<br>

## 4. 사용자 경험

- 사용자는 환전 요청의 최종 성공/실패 여부에만 관심이 있으며, 내부적인 보상 트랜잭션의 완료 여부를 실시간으로 알 필요는 없음

<br>

# ❗ 해결: Kafka를 통한 비동기 메시지 발행

- FxBank 환전 서비스는 보상 트랜잭션(출금 취소)을 Kafka 기반의 비동기 메시지 발행 방식으로 구현하여 위 문제들을 해결

<br>

## 1. 구현

### 1.1. WithdrawalCancelKafkaPublisher

- 환전 서비스는 `cancelWithdrawal()` 을 호출해 출금 취소 요청을 Kafka 토픽으로 발행하고, 계좌 서비스의 응답을 기다리지 않고 다음 작업을 계속 진행
- 실제 출금 취소 작업은 Kafka 메시지를 구독하는 계좌 서비스(또는 관련 서비스)에서 비동기적으로 처리

```java
@MessagingAdapter
@RequiredArgsConstructor
public class WithdrawalCancelKafkaPublisher implements CancelWithdrawalPort {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final WithdrawalCancelTopicMapper topicMapper;

    @Override
    public void cancelWithdrawal(ExchangeId exchangeId, Direction direction) {
        var message    = new WithdrawalCancelRequest(exchangeId);
        var serialized = MessageSerializer.serializeMessage(message);
        
        var requestTopic  = topicMapper.getRequestTopic(direction);
        var responseTopic = topicMapper.getResponseTopic(direction);
        
        var kafkaMessage = MessageBuilder.withPayload(serialized)
                .setHeader(KafkaHeaders.TOPIC, requestTopic)
                .setHeader(KafkaHeaders.REPLY_TOPIC, responseTopic)
                .setHeader(KafkaHeaders.KEY, exchangeId.value())
                .build();

        kafkaTemplate.send(kafkaMessage);
    }
    // ... (생략) ...
}
```

<br>

## 2. 기술 선택의 이유 및 장점

### 2.1. 서비스 간 결합도 감소

- 환전 서비스와 계좌 서비스가 Kafka 메시지 브로커를 통해 간접적으로 통신
- 직접적인 HTTP 호출과 달리 서비스 간의 시간적, 공간적 결합도가 감소하여 한 서비스의 장애가 다른 서비스에 직접적인 영향을 미치는 것을 방지

<br>

### 2.2. 장애 전파 최소화

- 계좌 서비스가 일시적으로 다운되더라도 환전 서비스는 메시지를 Kafka에 발행하고 작업을 계속 진행
- 계좌 서비스 복구 시 Kafka에 쌓인 메시지를 처리함으로써 장애 전파를 방지하고 시스템 가용성을 유지

<br>

### 2.3. 확장성 및 유연성

- Kafka의 높은 처리량과 확장성을 통해 메시지 볼륨 증가 시에도 안정적인 처리가 가능
- 새로운 서비스가 출금 취소 메시지를 구독하여 추가 로직을 수행하는 등 시스템 확장이 용이

<br>

### 2.4. 비동기 처리의 효율성

- 사용자에게 즉각적인 응답이 불필요한 작업에 비동기 통신을 적용하여 시스템 리소스를 효율적으로 사용하고 전체 처리량을 향상

<br>

## 3. 단점 및 트레이드오프

### 3.1. 복잡성 증가

- 메시지 브로커(Kafka) 도입으로 인해 시스템 아키텍처의 복잡성이 증가
- 메시지 발행/구독, 토픽 관리, 메시지 포맷 정의 등 추가적인 고려사항이 발생

<br>

### 3.2. 결과적 일관성

- 비동기 통신의 본질적인 특성상 결과적 일관성을 가짐
- 메시지 발행 시점과 실제 처리 시점 사이에 지연이 발생할 수 있으며, 시스템 상태의 즉각적인 일관성을 보장하지 않음

<br>

### 3.3. 메시지 처리 보장

- 메시지 유실 없이 정확히 한 번만 처리되도록 보장하는 메커니즘(At-Least-Once, Exactly-Once) 구현의 중요성이 커짐
- Kafka는 다양한 기능을 제공하지만, 개발자의 올바른 활용이 필요

<br>

## 4. 다른 기술과의 비교 및 적합성

### 4.1. HTTP 동기 통신 vs Kafka 비동기 통신

- 사용자에게 즉각적인 응답이 필요한 핵심 비즈니스 로직(예: 환전 요청 접수)에는 HTTP 동기 통신이 적합
- 반면, 백그라운드 처리가 가능하거나 서비스 간 결합도 감소 및 장애 내성 향상이 필요한 작업(예: 보상 트랜잭션, 이벤트 기반 통신)에는 Kafka와 같은 메시지 큐 기반의 비동기 통신이 효과적

<br>

### 4.2. 다른 메시지 큐

- RabbitMQ, ActiveMQ 등 다양한 메시지 큐가 존재
- Kafka는 높은 처리량, 내구성, 확장성, 스트리밍 데이터 처리 능력으로 인해 대규모 분산 시스템에서 널리 사용
- FxBank 환전 서비스는 Kafka의 이러한 장점을 활용하여 안정적인 비동기 통신 인프라를 구축
- FxBank 환전 서비스는 비동기 통신을 통해 서비스 간의 느슨한 결합을 달성하고, 외부 서비스 장애가 전체 시스템으로 전파되는 것을 효과적으로 방지

<br>

# ❓ 문제: 전송 오류로 인한 순서 뒤바뀜과 중복 처리

- Kafka의 "At Least Once" 전송 방식은 네트워크 장애나 브로커 응답 지연 시, 프로듀서가 메시지를 재전송하게 만듦

<br>

## 1. 메시지 중복

- 동일한 메시지가 브로커에 여러 번 적재되어, 컨슈머가 중복 처리할 위험이 생김
- 예: 환전 취소 요청이 이중으로 처리되어 돈이 두 번 입금됨

<br>

## 2. 순서 뒤바뀜

- 첫 번째 메시지 배치가 전송 실패 후 재시도되는 동안, 두 번째 메시지 배치가 먼저 성공적으로 브로커에 기록될 경우, 메시지의 논리적 순서가 꼬이게 됨

<br>

# ❗ 해결: 멱등성 프로듀서와 메시지 키 활용

- 프로듀서의 멱등성 옵션을 활성화하여 재시도로 인한 중복과 순서 변경 문제를 한 번에 해결
- 메시지 키를 사용하여 특정 거래와 관련된 메시지들의 처리 순서를 보장

<br>

## 1. 구현 상세

### 1.1. **멱등성 프로듀서** 옵션 활성화

- 프로듀서는 각 메시지에 고유 ID(PID, 시퀀스 번호)를 할당하고, 브로커는 이 ID를 통해 중복된 메시지를 감지하고 저장하지 않음
- 또한, 이 옵션은 `max.in.flight.requests.per.connection` 값을 5 이하로 강제하여, 프로듀서가 이전 배치의 성공 여부를 확인하기 전에 다음 배치를 보내지 않도록 막아 메시지 순서를 보장함

```yaml
# application.yml
spring:
  kafka:
    producer:
      # 멱등성 프로듀서 활성화로 중복 제거 및 순서 보장
      enable.idempotence: true
```

> **Note:** `enable.idempotence: true`로 설정하면 `acks`는 자동으로 `all`로, `retries`는 `Integer.MAX_VALUE`로 조정되어 메시지 유실 방지에도 기여함

<br>

### 1.2. 메시지 키: 동일 파티션으로의 라우팅

- 메시지를 보낼 때 `exchangeId`와 같은 고유한 키를 지정함
- Kafka는 키의 해시값을 기준으로 파티션을 결정하므로, 동일한 키를 가진 메시지들은 항상 같은 파티션으로 전송됨
- 파티션 내에서는 메시지 순서가 보장되므로, 특정 거래와 관련된 일련의 이벤트들이 컨슈머에 의해 순서대로 처리됨

<br>

# ❓ 문제: 메시지 유실 가능성

- 프로듀서가 보낸 메시지가 브로커에 안전하게 저장되기 전에 네트워크가 끊기거나, 컨슈머가 메시지를 처리하고 커밋하기 전에 장애가 발생하면 메시지가 유실될 수 있음
- 금융 거래에서 메시지 유실은 곧 금전적 손실로 이어짐

<br>

# ❗ 해결: 프로듀서 acks, retry, delivery.timeout.ms 설정과 컨슈머 수동 커밋

- 프로듀서와 컨슈머의 설정을 최적화하여 메시지가 안전하게 처리되고 저장되도록 보장

<br>

## 1. 구현 상세

### 1.1. 프로듀서: application.yml 설정

```yaml
spring:
  kafka:
    producer:
      properties:
        acks: all # 모든 ISR로부터 확인 응답을 받도록 설정
        retries: 2147483647 # 메시지 전송 실패 시 재시도 횟수 (enable.idempotence=true 시 Integer.MAX_VALUE로 자동 설정)
        delivery.timeout.ms: 120000 # 메시지 전송 및 응답 대기 최대 시간 (ms)
```

<br>

#### 1.1.1. acks=all

- 프로듀서가 메시지를 보낸 후, 리더 파티션뿐만 아니라 모든 ISR(In-Sync Replicas)로부터 확인 응답을 받을 때까지 기다리도록 설정함
- 이는 브로커 하나에 장애가 발생하더라도 메시지가 유실되지 않도록 보장하는 가장 안전한 옵션임

<br>

#### 1.1.2. retries

- 메시지 전송 실패 시 재시도 횟수를 설정함
- 네트워크 불안정 등으로 인한 일시적인 오류에 대응하여 메시지 유실 가능성을 최소화함

<br>

#### 1.1.3. delivery.timeout.ms

- 프로듀서가 메시지를 전송하고 브로커로부터 응답을 받기까지 기다리는 최대 시간을 설정함
- 이 시간 내에 응답을 받지 못하면 전송 실패로 간주하고 재시도 로직을 수행함

<br>

### 1.2. 컨슈머: 수동 커밋

#### 1.2.1. application.yml 설정

- 자동 커밋을 비활성화

```yaml
# application.yml
spring:
  kafka:
    consumer:
      enable.auto.commit: false # 자동 커밋 비활성화
```

<br>

#### 1.2.2. WithdrawalCancelKafkaConsumer

- 비즈니스 로직이 성공적으로 완료되었을 때만 명시적으로 오프셋을 커밋하여 메시지 처리의 정확성을 보장함

```java
@MessagingAdapter
@RequiredArgsConstructor
public class WithdrawalCancelKafkaConsumer {

    private final CompleteWithdrawalCancelUseCase completeWithdrawalCancelUseCase;

    @KafkaListener(
            topics  = "${kafka.fx-account.withdrawal-cancel-response-topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void fromFxAccount(String message, Acknowledgment ack) {
        fromAccount(message, Direction.SELL, ack);
    }

    private void fromAccount(String payload, Direction direction, Acknowledgment ack) {
        try {
            var command = toCommand(payload, direction);
            completeWithdrawalCancelUseCase.completeWithdrawalCancel(command);
            ack.acknowledge();
            
        } catch (Exception e) {
            throw e;
        }
    }
    
    // ... (생략) ...
}

```

<br>

### 1.3. 컨슈머: Dead Letter Topic (DLT)

- 반복적으로 처리에 실패하는 메시지가 전체 시스템을 막는 것을 방지하고, 동시에 해당 메시지를 유실하지 않기 위해 DLT(Dead Letter Topic) 패턴을 적용
- 재시도에 실패한 메시지는 DLT로 전송되어 나중에 원인을 분석하고 수동으로 처리할 수 있음

```java
// KafkaConfig.java
@Bean
public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
        ConsumerFactory<String, String> consumerFactory,
        KafkaTemplate<String, String> kafkaTemplate) {
    
    ConcurrentKafkaListenerContainerFactory<String, String> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    
    // ... (생략) ...

    // DLT(Dead Letter Topic)로 실패한 메시지 전송
    DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
    // 1초 간격으로 3번 재시도 후 DLT로 전송
    DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3));

    factory.setCommonErrorHandler(errorHandler);
    
    return factory;
}
```