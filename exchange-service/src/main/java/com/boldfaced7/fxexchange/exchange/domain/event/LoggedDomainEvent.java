package com.boldfaced7.fxexchange.exchange.domain.event;

import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

import java.time.LocalDateTime;

public interface LoggedDomainEvent extends DomainEvent {
    RequestId requestId();
    LocalDateTime raisedAt();
}
