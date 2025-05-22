package com.boldfaced7.fxexchange.exchange.application.service;

import com.boldfaced7.fxexchange.common.UseCase;
import com.boldfaced7.fxexchange.exchange.application.port.in.BuyForeignCurrencyCommand;
import com.boldfaced7.fxexchange.exchange.application.port.in.BuyForeignCurrencyUseCase;
import com.boldfaced7.fxexchange.exchange.application.port.out.DepositFxPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.SaveExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.UpdateExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.WithdrawKrwPort;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class BuyForeignCurrencyService implements BuyForeignCurrencyUseCase {

    private final WithdrawKrwPort withdrawKrwPort;
    private final DepositFxPort depositFxPort;
    private final SaveExchangeRequestPort saveExchangeRequestPort;
    private final UpdateExchangeRequestPort updateExchangeRequestPort;

    @Override
    public ExchangeRequest buy(BuyForeignCurrencyCommand command) {
        var toBeSaved = toRequest(command);
        var exchangeRequest = saveExchangeRequestPort.save(toBeSaved);

        withdrawKrwPort.withdrawKrw(exchangeRequest)
                .ifPresent(exchangeRequest::registerWithdrawId);

        depositFxPort.depositFx(exchangeRequest)
                .ifPresent(exchangeRequest::registerDepositId);

        return updateExchangeRequestPort.update(exchangeRequest);
    }

    private ExchangeRequest toRequest(BuyForeignCurrencyCommand command) {
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
