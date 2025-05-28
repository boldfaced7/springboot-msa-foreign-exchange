package com.boldfaced7.fxexchange.exchange.application.log;

import com.boldfaced7.fxexchange.exchange.application.port.out.log.SaveExchangeStateLogPort;
import com.boldfaced7.fxexchange.exchange.domain.enums.ExchangeState;
import com.boldfaced7.fxexchange.exchange.domain.event.LoggedDomainEvent;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeStateLog;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ExchangeStateLogger {

    private final SaveExchangeStateLogPort saveExchangeStateLogPort;

    @Async
    @TransactionalEventListener
    public void log(LoggedDomainEvent event) {
        saveExchangeStateLogPort.save(toModel(event));
    }

    ExchangeStateLog toModel(LoggedDomainEvent event) {
        return ExchangeStateLog.of(
                event.requestId(),
                ExchangeState.fromEventClass(event.getClass()),
                event.raisedAt()
        );
    }
}
