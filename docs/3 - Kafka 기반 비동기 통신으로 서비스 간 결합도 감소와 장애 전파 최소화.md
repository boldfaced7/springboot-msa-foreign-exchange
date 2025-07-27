# ❓ 문제: 보상 트랜잭션의 동기 처리 비효율성 및 위험성

- 환전 서비스는 입금 실패 시 '원화 출금 취소' 보상 트랜잭션을 실행
- 만약 이 보상 트랜잭션까지 동기적으로 처리한다면 다음과 같은 문제가 발생할 수 있음

</br></br>

## 1. 불필요한 리소스 점유

- 환전 서비스가 계좌 서비스의 출금 취소 응답을 기다리는 동안 불필요하게 리소스를 점유하게 됨
- 이는 다른 환전 요청 처리에도 영향을 미쳐 전체 시스템의 처리량을 저하시킬 수 있음

</br></br>

## 2. 강한 의존성

- 환전 서비스가 계좌 서비스에 직접적으로 의존하게 되어, 계좌 서비스의 장애나 지연이 환전 서비스에 직접적인 영향을 미치게 됨

</br></br>

## 3. 장애 전파 위험

- 보상 트랜잭션 과정에서 계좌 서비스에 문제가 발생할 경우, 그 장애가 환전 서비스로 전파되어 환전 서비스의 안정성을 위협할 수 있음

</br></br>

## 4. 사용자 경험

- 사용자는 환전 요청의 최종 성공/실패 여부에만 관심이 있으며, 내부적인 보상 트랜잭션의 완료 여부를 실시간으로 알 필요는 없음

</br></br>

# ❗ 해결: Kafka를 통한 비동기 메시지 발행

- FxBank 환전 서비스는 보상 트랜잭션(출금 취소)을 Kafka 기반의 비동기 메시지 발행 방식으로 구현하여 위 문제들을 해결

</br></br>

## 1. 구현

### 1.1. WithdrawalCancelKafkaPublisher

- `exchange-service`의 `WithdrawalCancelKafkaPublisher`는 `CancelWithdrawalPort` 인터페이스를 구현
- 출금 취소 요청을 Kafka 토픽으로 발행하는 역할을 담당

```java
@MessagingAdapter
@RequiredArgsConstructor
public class WithdrawalCancelKafkaPublisher implements CancelWithdrawalPort {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final WithdrawalCancelTopicMapper topicMapper;

    @Override
    public void cancelWithdrawal(ExchangeId exchangeId, Direction direction) {
        var requestTopic = topicMapper.getRequestTopic(direction);
        var responseTopic = topicMapper.getResponseTopic(direction);
        var message = new WithdrawalCancelRequest(exchangeId);
        var serialized = MessageSerializer.serializeMessage(message);

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

- 환전 서비스는 `cancelWithdrawal` 메서드 호출을 통해 출금 취소 요청을 Kafka 토픽으로 발행하고, 계좌 서비스의 응답을 기다리지 않고 다음 작업을 계속 진행
- 실제 출금 취소 작업은 Kafka 메시지를 구독하는 계좌 서비스(또는 관련 서비스)에서 비동기적으로 처리

</br></br>

## 2. 기술 선택의 이유 및 장점

### 2.1. 서비스 간 결합도 감소

- 환전 서비스와 계좌 서비스가 Kafka 메시지 브로커를 통해 간접적으로 통신
- 직접적인 HTTP 호출과 달리 서비스 간의 시간적, 공간적 결합도가 감소하여 한 서비스의 장애가 다른 서비스에 직접적인 영향을 미치는 것을 방지

</br></br>

### 2.2. 장애 전파 최소화

- 계좌 서비스가 일시적으로 다운되더라도 환전 서비스는 메시지를 Kafka에 발행하고 작업을 계속 진행
- 계좌 서비스 복구 시 Kafka에 쌓인 메시지를 처리함으로써 장애 전파를 방지하고 시스템 가용성을 유지

</br></br>

### 2.3. 확장성 및 유연성

- Kafka의 높은 처리량과 확장성을 통해 메시지 볼륨 증가 시에도 안정적인 처리가 가능
- 새로운 서비스가 출금 취소 메시지를 구독하여 추가 로직을 수행하는 등 시스템 확장이 용이

</br></br>

### 2.4. 비동기 처리의 효율성

- 사용자에게 즉각적인 응답이 불필요한 작업에 비동기 통신을 적용하여 시스템 리소스를 효율적으로 사용하고 전체 처리량을 향상

</br></br>

## 3. 단점 및 트레이드오프

### 3.1. 복잡성 증가

- 메시지 브로커(Kafka) 도입으로 인해 시스템 아키텍처의 복잡성이 증가
- 메시지 발행/구독, 토픽 관리, 메시지 포맷 정의 등 추가적인 고려사항이 발생

</br></br>

### 3.2. 결과적 일관성

- 비동기 통신의 본질적인 특성상 결과적 일관성을 가짐
- 메시지 발행 시점과 실제 처리 시점 사이에 지연이 발생할 수 있으며, 시스템 상태의 즉각적인 일관성을 보장하지 않음

</br></br>

### 3.3. 메시지 처리 보장

- 메시지 유실 없이 정확히 한 번만 처리되도록 보장하는 메커니즘(At-Least-Once, Exactly-Once) 구현의 중요성이 커짐
- Kafka는 다양한 기능을 제공하지만, 개발자의 올바른 활용이 필요

</br></br>

## 4. 다른 기술과의 비교 및 적합성

### 4.1. HTTP 동기 통신 vs. Kafka 비동기 통신

- 사용자에게 즉각적인 응답이 필요한 핵심 비즈니스 로직(예: 환전 요청 접수)에는 HTTP 동기 통신이 적합
- 반면, 백그라운드 처리가 가능하거나 서비스 간 결합도 감소 및 장애 내성 향상이 필요한 작업(예: 보상 트랜잭션, 이벤트 기반 통신)에는 Kafka와 같은 메시지 큐 기반의 비동기 통신이 효과적

</br></br>

### 4.2. 다른 메시지 큐

- RabbitMQ, ActiveMQ 등 다양한 메시지 큐가 존재
- Kafka는 높은 처리량, 내구성, 확장성, 스트리밍 데이터 처리 능력으로 인해 대규모 분산 시스템에서 널리 사용
- FxBank 환전 서비스는 Kafka의 이러한 장점을 활용하여 안정적인 비동기 통신 인프라를 구축

- FxBank 환전 서비스는 비동기 통신을 통해 서비스 간의 느슨한 결합을 달성하고, 외부 서비스 장애가 전체 시스템으로 전파되는 것을 효과적으로 방지
- 이를 통해 시스템의 안정성과 확장성을 동시에 확보하는 중요한 아키텍처 결정을 내림