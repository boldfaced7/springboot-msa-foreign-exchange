package com.boldfaced7.fxexchange.exchange.application.service;

import com.boldfaced7.fxexchange.common.UseCase;
import com.boldfaced7.fxexchange.exchange.application.port.in.SellForeignCurrencyCommand;
import com.boldfaced7.fxexchange.exchange.application.port.in.SellForeignCurrencyUseCase;
import com.boldfaced7.fxexchange.exchange.application.port.out.DepositKrwPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.SaveExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.UpdateExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.WithdrawFxPort;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class SellForeignCurrencyService implements SellForeignCurrencyUseCase {

    private final WithdrawFxPort withdrawFxPort;
    private final DepositKrwPort depositKrwPort;
    private final SaveExchangeRequestPort saveExchangeRequestPort;
    private final UpdateExchangeRequestPort updateExchangeRequestPort;

    @Override
    public ExchangeRequest sell(SellForeignCurrencyCommand command) {
        var toBeSaved = toRequest(command);
        var exchangeRequest = saveExchangeRequestPort.save(toBeSaved);

        withdrawFxPort.withdrawFx(exchangeRequest)
                .ifPresent(exchangeRequest::registerWithdrawId);

        depositKrwPort.depositKrw(exchangeRequest)
                .ifPresent(exchangeRequest::registerDepositId);

        return updateExchangeRequestPort.update(exchangeRequest);
    }

    private ExchangeRequest toRequest(SellForeignCurrencyCommand command) {
        return ExchangeRequest.of(
                command.exchangeId(),
                command.userId(),
                command.direction(),
                command.baseCurrency(),
                command.quoteCurrency(),
                command.baseAmount(),
                command.quoteAmount(),
                command.exchangeRate()
        );
    }
}
