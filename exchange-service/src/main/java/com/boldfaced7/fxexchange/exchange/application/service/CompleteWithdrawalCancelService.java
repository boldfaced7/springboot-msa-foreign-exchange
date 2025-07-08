package com.boldfaced7.fxexchange.exchange.application.service;

import com.boldfaced7.fxexchange.common.UseCase;
import com.boldfaced7.fxexchange.exchange.application.port.in.CompleteWithdrawalCancelCommand;
import com.boldfaced7.fxexchange.exchange.application.port.in.CompleteWithdrawalCancelUseCase;
import com.boldfaced7.fxexchange.exchange.application.port.out.cache.LoadExchangeRequestCachePort;
import com.boldfaced7.fxexchange.exchange.application.port.out.cancel.SaveWithdrawalCancelPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.event.PublishEventPort;
import com.boldfaced7.fxexchange.exchange.domain.exception.ExchangeRequestNotFoundException;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.model.WithdrawalCancel;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class CompleteWithdrawalCancelService implements CompleteWithdrawalCancelUseCase {

    private final LoadExchangeRequestCachePort loadExchangeRequestCachePort;
    private final SaveWithdrawalCancelPort saveWithdrawalCancelPort;
    private final PublishEventPort publishEventPort;

    @Override
    @Transactional
    public void completeWithdrawalCancel(CompleteWithdrawalCancelCommand command) {
        var exchange = getExchange(command.exchangeId());
        var withdrawalCancel = toDomain(exchange, command);

        publishEventPort.publish(withdrawalCancel);
        saveWithdrawalCancelPort.saveWithdrawalCancel(withdrawalCancel);
    }

    private ExchangeRequest getExchange(ExchangeId exchangeId) {
        return loadExchangeRequestCachePort.loadByExchangeId(exchangeId)
                .orElseThrow(() -> new ExchangeRequestNotFoundException(exchangeId));
    }

    private WithdrawalCancel toDomain(
            ExchangeRequest exchange,
            CompleteWithdrawalCancelCommand command
    ) {
        return WithdrawalCancel.create(
                command.withdrawalCancelId(),
                exchange.getRequestId(),
                command.exchangeId(),
                exchange.getUserId(),
                command.direction(),
                true
        );
    }
}
