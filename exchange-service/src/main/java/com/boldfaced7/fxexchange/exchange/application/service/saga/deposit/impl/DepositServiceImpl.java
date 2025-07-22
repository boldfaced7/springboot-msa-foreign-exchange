package com.boldfaced7.fxexchange.exchange.application.service.saga.deposit.impl;

import com.boldfaced7.fxexchange.exchange.application.port.aop.Idempotent;
import com.boldfaced7.fxexchange.exchange.application.port.out.cache.SaveExchangeRequestCachePort;
import com.boldfaced7.fxexchange.exchange.application.port.out.deposit.RequestDepositPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.deposit.SaveDepositPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.event.PublishEventPort;
import com.boldfaced7.fxexchange.exchange.application.service.saga.deposit.DepositService;
import com.boldfaced7.fxexchange.exchange.domain.model.Deposit;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.deposit.DepositDetail;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DepositServiceImpl implements DepositService {

    private final RequestDepositPort requestDepositPort;
    private final SaveDepositPort saveDepositPort;
    private final SaveExchangeRequestCachePort saveExchangeRequestCachePort;
    private final PublishEventPort publishEventPort;

    @Override
    @Transactional
    @Idempotent(prefix = "deposit:request:", key = "#exchange.exchangeId")
    public DepositDetail deposit(ExchangeRequest exchange) {
        var deposit = requestDeposit(exchange);

        publishDepositEvent(deposit);
        saveDeposit(deposit, exchange);

        return new DepositDetail(exchange, deposit);
    }

    private Deposit requestDeposit(ExchangeRequest exchange) {
        try {
            return requestDepositPort.deposit(exchange);

        } catch (Exception e) {
            saveExchangeRequestCachePort.save(exchange);
            exchange.markDepositUnknown(Count.zero());
            publishEventPort.publish(exchange);
            throw e;
        }
    }

    private void publishDepositEvent(Deposit deposit) {
        deposit.processTransactionResult();
        publishEventPort.publish(deposit);
    }

    private void saveDeposit(Deposit deposit, ExchangeRequest exchange) {
        if (!deposit.isSuccess()) {
            saveExchangeRequestCachePort.save(exchange);
            throw new IllegalStateException("입금 실패");
        }
        saveDepositPort.saveDeposit(deposit);
    }

}
