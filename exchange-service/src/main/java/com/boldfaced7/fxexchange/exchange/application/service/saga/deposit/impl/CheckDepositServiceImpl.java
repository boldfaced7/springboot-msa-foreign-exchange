package com.boldfaced7.fxexchange.exchange.application.service.saga.deposit.impl;

import com.boldfaced7.fxexchange.exchange.application.port.out.cache.LoadExchangeRequestCachePort;
import com.boldfaced7.fxexchange.exchange.application.port.out.deposit.LoadDepositPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.deposit.SaveDepositPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.event.PublishEventPort;
import com.boldfaced7.fxexchange.exchange.application.service.saga.deposit.CheckDepositService;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.RetryPolicy;
import com.boldfaced7.fxexchange.exchange.domain.exception.ExchangeRequestNotFoundException;
import com.boldfaced7.fxexchange.exchange.domain.model.Deposit;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.RequestId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CheckDepositServiceImpl implements CheckDepositService {

    private final LoadExchangeRequestCachePort loadExchangeRequestCachePort;
    private final LoadDepositPort loadDepositPort;
    private final SaveDepositPort saveDepositPort;
    private final PublishEventPort publishEventPort;
    private final RetryPolicy retryPolicy;

    @Override
    @Transactional
    public void checkDeposit(RequestId requestId, Count count) {
        var exchange = getExchange(requestId);
        var deposit = getDeposit(exchange, count);

        publishDepositEvent(deposit);
        saveDeposit(deposit);
    }

    private ExchangeRequest getExchange(RequestId requestId) {
        return loadExchangeRequestCachePort.loadByRequestId(requestId)
                .orElseThrow(() -> new ExchangeRequestNotFoundException(requestId));
    }

    private Deposit getDeposit(ExchangeRequest exchange, Count count) {
        try {
            return loadDepositPort.loadDeposit(exchange);

        } catch (Exception e) {
            exchange.handleDepositCheckUnknown(count, retryPolicy);
            publishEventPort.publish(exchange);
            throw e;
        }
    }

    private void publishDepositEvent(Deposit deposit) {
        deposit.processCheckResult();
        publishEventPort.publish(deposit);
    }

    private void saveDeposit(Deposit deposit) {
        if (deposit.isSuccess()) {
            saveDepositPort.saveDeposit(deposit);
        }
    }

}
