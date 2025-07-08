package com.boldfaced7.fxexchange.exchange.application.service;

import com.boldfaced7.fxexchange.exchange.application.port.in.LogExchangeStateCommand;
import com.boldfaced7.fxexchange.exchange.application.port.in.LogExchangeStateUseCase;
import com.boldfaced7.fxexchange.exchange.application.port.out.log.SaveExchangeStateLogPort;
import com.boldfaced7.fxexchange.exchange.domain.enums.ExchangeState;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeStateLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogExchangeStateService implements LogExchangeStateUseCase {

    private final SaveExchangeStateLogPort saveExchangeStateLogPort;

    // EXCHANGE_CURRENCY_STARTED
    @Override
    public void logExchangeCurrencyStarted(LogExchangeStateCommand command) {
        log.info("EXCHANGE_CURRENCY_STARTED: {}", command);
        saveLog(command, ExchangeState.EXCHANGE_CURRENCY_STARTED);
    }

    // EXCHANGE_CURRENCY_SUCCEEDED
    @Override
    public void logExchangeCurrencySucceeded(LogExchangeStateCommand command) {
        log.info("EXCHANGE_CURRENCY_SUCCEEDED: {}", command);
        saveLog(command, ExchangeState.EXCHANGE_CURRENCY_SUCCEEDED);
    }

    // EXCHANGE_CURRENCY_FAILED
    @Override
    public void logExchangeCurrencyFailed(LogExchangeStateCommand command) {
        log.info("EXCHANGE_CURRENCY_FAILED: {}", command);
        saveLog(command, ExchangeState.EXCHANGE_CURRENCY_FAILED);
    }

    // WITHDRAWAL_SUCCEEDED
    @Override
    public void logWithdrawalSucceeded(LogExchangeStateCommand command) {
        log.info("WITHDRAWAL_SUCCEEDED: {}", command);
        saveLog(command, ExchangeState.WITHDRAWAL_SUCCEEDED);
    }

    // CHECKING_WITHDRAWAL_REQUIRED
    @Override
    public void logWithdrawalUnknown(LogExchangeStateCommand command) {
        log.info("CHECKING_WITHDRAWAL_REQUIRED: {}", command);
        saveLog(command, ExchangeState.CHECKING_WITHDRAWAL_REQUIRED);
    }

    // CHECKING_DEPOSIT_REQUIRED
    @Override
    public void logDepositUnknown(LogExchangeStateCommand command) {
        log.info("CHECKING_DEPOSIT_REQUIRED: {}", command);
        saveLog(command, ExchangeState.CHECKING_DEPOSIT_REQUIRED);
    }

    // CANCELING_WITHDRAWAL_REQUIRED
    @Override
    public void logWithdrawalSuccessChecked(LogExchangeStateCommand command) {
        log.info("CHECKING_WITHDRAWAL_REQUIRED: {}", command);
        saveLog(command, ExchangeState.CANCELING_WITHDRAWAL_REQUIRED);
    }

    // CANCELING_WITHDRAWAL_REQUIRED
    @Override
    public void logDepositFailureChecked(LogExchangeStateCommand command) {
        log.info("CHECKING_WITHDRAWAL_REQUIRED: {}", command);
        saveLog(command, ExchangeState.CANCELING_WITHDRAWAL_REQUIRED);
    }

    // CANCELING_WITHDRAWAL_REQUIRED
    @Override
    public void logDepositFailed(LogExchangeStateCommand command) {
        saveLog(command, ExchangeState.CANCELING_WITHDRAWAL_REQUIRED);
    }

    private void saveLog(LogExchangeStateCommand command, ExchangeState exchangeState) {
        ExchangeStateLog exchangeStateLog = toModel(command, exchangeState);
        saveExchangeStateLogPort.save(exchangeStateLog);
    }

    private ExchangeStateLog toModel(LogExchangeStateCommand command, ExchangeState exchangeState) {
        return ExchangeStateLog.of(
                command.requestId(),
                command.direction(),
                exchangeState,
                command.raisedAt()
        );
    }

}
