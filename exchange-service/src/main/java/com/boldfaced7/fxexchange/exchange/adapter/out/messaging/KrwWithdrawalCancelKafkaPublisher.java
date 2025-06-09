package com.boldfaced7.fxexchange.exchange.adapter.out.messaging;

import com.boldfaced7.fxexchange.common.MessagingAdapter;
import com.boldfaced7.fxexchange.exchange.application.port.out.CancelWithdrawalPort;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
@Profile("!test")
@MessagingAdapter
@RequiredArgsConstructor
public class KrwWithdrawalCancelKafkaPublisher implements
        CancelWithdrawalPort
{
    private static final String TOPIC_KRW_WITHDRAWAL_CANCEL = "exchange-topic.krw-withdrawal.cancel";

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void cancelWithdrawal(ExchangeId exchangeId) {
        kafkaTemplate.send(TOPIC_KRW_WITHDRAWAL_CANCEL, exchangeId.value());
    }

    @Override
    public Direction direction() {
        return Direction.BUY;
    }
}
