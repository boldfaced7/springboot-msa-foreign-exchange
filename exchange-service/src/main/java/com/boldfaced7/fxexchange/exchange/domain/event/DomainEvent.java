package com.boldfaced7.fxexchange.exchange.domain.event;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

import java.time.LocalDateTime;

public interface DomainEvent {
    Direction direction();
    RequestId requestId();
    ExchangeId exchangeId();
    LocalDateTime raisedAt();
}
