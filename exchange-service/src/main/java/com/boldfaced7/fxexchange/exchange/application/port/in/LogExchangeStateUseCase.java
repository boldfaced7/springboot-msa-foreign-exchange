package com.boldfaced7.fxexchange.exchange.application.port.in;

public interface LogExchangeStateUseCase {
    // EXCHANGE_CURRENCY_STARTED
    void logExchangeCurrencyStarted(LogExchangeStateCommand command);

    // EXCHANGE_CURRENCY_SUCCEEDED
    void logExchangeCurrencySucceeded(LogExchangeStateCommand command);

    // EXCHANGE_CURRENCY_FAILED
    void logExchangeCurrencyFailed(LogExchangeStateCommand command);

    // WITHDRAWAL_SUCCEEDED
    void logWithdrawalSucceeded(LogExchangeStateCommand command);

    // CHECKING_WITHDRAWAL_REQUIRED
    void logWithdrawalUnknown(LogExchangeStateCommand command);

    // CHECKING_DEPOSIT_REQUIRED
    void logDepositUnknown(LogExchangeStateCommand command);

    // CANCELING_WITHDRAWAL_REQUIRED
    void logWithdrawalSuccessChecked(LogExchangeStateCommand command);

    // CANCELING_WITHDRAWAL_REQUIRED
    void logDepositFailureChecked(LogExchangeStateCommand command);

    // CANCELING_WITHDRAWAL_REQUIRED
    void logDepositFailed(LogExchangeStateCommand command);
}
