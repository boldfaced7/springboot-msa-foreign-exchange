package com.boldfaced7.fxexchange.exchange.adapter.out.external.account;

import com.boldfaced7.fxexchange.exchange.adapter.out.AccountTransactionClientMapper;
import com.boldfaced7.fxexchange.exchange.application.port.out.LoadDepositResultPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.LoadWithdrawalResultPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.RequestDepositPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.RequestWithdrawalPort;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountTransactionAdapter implements
        RequestDepositPort,
        LoadDepositResultPort,
        RequestWithdrawalPort,
        LoadWithdrawalResultPort
{
    private final AccountTransactionClientMapper clientMapper;

    @Override
    public DepositResult loadDepositResult(ExchangeId exchangeId, Direction direction) {
        return clientMapper.getDepositResultClient(direction)
                .loadDepositResult(exchangeId)
                .map(AccountTransactionAdapter::toDepositResult)
                .block();
    }

    @Override
    public WithdrawalResult loadWithdrawalResult(ExchangeId exchangeId, Direction direction) {
        return clientMapper.getWithdrawalResultClient(direction)
                .loadWithdrawalResult(exchangeId)
                .map(AccountTransactionAdapter::toWithdrawalResult)
                .block();
    }

    @Override
    public DepositResult deposit(ExchangeRequest requested) {
        return clientMapper.getDepositClient(requested.getDirection())
                .requestDeposit(requested)
                .map(AccountTransactionAdapter::toDepositResult)
                .block();
    }

    @Override
    public WithdrawalResult withdraw(ExchangeRequest requested) {
        return clientMapper.getWithdrawalClient(requested.getDirection())
                .requestWithdrawal(requested)
                .map(AccountTransactionAdapter::toWithdrawalResult)
                .block();
    }

    private static DepositResult toDepositResult(TransactionResponse transactionResponse) {
        return new DepositResult(
                transactionResponse.success(),
                new AccountCommandStatus(transactionResponse.status()),
                (transactionResponse.transactionId() != null)
                        ? new DepositId(transactionResponse.transactionId())
                        : null
        );
    }

    private static WithdrawalResult toWithdrawalResult(TransactionResponse transactionResponse) {
        log.info("transaction response: {}", transactionResponse);
        return new WithdrawalResult(
                transactionResponse.success(),
                new AccountCommandStatus(transactionResponse.status()),
                (transactionResponse.transactionId() != null)
                        ? new WithdrawalId(transactionResponse.transactionId())
                        : null
        );
    }

}
