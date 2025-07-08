package com.boldfaced7.fxexchange.exchange.adapter.out.external.account;

import com.boldfaced7.fxexchange.exchange.application.port.out.deposit.LoadDepositPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.deposit.RequestDepositPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.withdrawal.LoadWithdrawalPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.withdrawal.RequestWithdrawalPort;
import com.boldfaced7.fxexchange.exchange.domain.model.Deposit;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.model.Withdrawal;
import com.boldfaced7.fxexchange.exchange.domain.vo.DepositId;
import com.boldfaced7.fxexchange.exchange.domain.vo.WithdrawalId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountTransactionAdapter implements
        RequestDepositPort,
        LoadDepositPort,
        RequestWithdrawalPort,
        LoadWithdrawalPort
{
    private final AccountTransactionClientMapper clientMapper;

    @Override
    public Deposit deposit(ExchangeRequest exchange) {
        return clientMapper.getDepositClient(exchange.getDirection())
                .requestDeposit(exchange)
                .map(response -> toDeposit(response, exchange))
                .block();
    }

    @Override
    public Deposit loadDeposit(ExchangeRequest exchange) {
        return clientMapper.getDepositResultClient(exchange.getDirection())
                .loadDepositResult(exchange.getExchangeId())
                .map(response -> toDeposit(response, exchange))
                .block();
    }

    @Override
    public Withdrawal withdraw(ExchangeRequest exchange) {
        return clientMapper.getWithdrawalClient(exchange.getDirection())
                .requestWithdrawal(exchange)
                .map(response -> toWithdrawal(response, exchange))
                .block();
    }

    @Override
    public Withdrawal loadWithdrawal(ExchangeRequest exchange) {
        return clientMapper.getWithdrawalResultClient(exchange.getDirection())
                .loadWithdrawalResult(exchange.getExchangeId())
                .map(response -> toWithdrawal(response, exchange))
                .block();
    }

    private static Deposit toDeposit(TransactionResponse transactionResponse, ExchangeRequest exchange) {
        return Deposit.create(
                (transactionResponse.transactionId() == null) ? null : new DepositId(transactionResponse.transactionId()),
                exchange.getRequestId(),
                exchange.getExchangeId(),
                exchange.getUserId(),
                exchange.getDirection(),
                transactionResponse.success()
        );
    }

    private static Withdrawal toWithdrawal(TransactionResponse transactionResponse, ExchangeRequest exchange) {
        return Withdrawal.create(
                (transactionResponse.transactionId() == null) ? null : new WithdrawalId(transactionResponse.transactionId()),
                exchange.getRequestId(),
                exchange.getExchangeId(),
                exchange.getUserId(),
                exchange.getDirection(),
                transactionResponse.success()
        );
    }


}
