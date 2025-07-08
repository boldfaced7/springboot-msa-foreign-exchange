package com.boldfaced7.fxexchange.exchange.application.service.saga.withdrawal.impl;

import com.boldfaced7.fxexchange.exchange.application.port.out.cache.LoadExchangeRequestCachePort;
import com.boldfaced7.fxexchange.exchange.application.port.out.event.PublishEventPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.withdrawal.LoadWithdrawalPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.withdrawal.SaveWithdrawalPort;
import com.boldfaced7.fxexchange.exchange.application.service.saga.withdrawal.CheckWithdrawalService;
import com.boldfaced7.fxexchange.exchange.domain.exception.ExchangeRequestNotFoundException;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.model.Withdrawal;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;
import com.boldfaced7.fxexchange.exchange.domain.vo.RetryPolicy;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckWithdrawalServiceImpl implements CheckWithdrawalService {

    private final LoadExchangeRequestCachePort loadExchangeRequestCachePort;
    private final LoadWithdrawalPort loadWithdrawalPort;
    private final SaveWithdrawalPort saveWithdrawalPort;
    private final PublishEventPort publishEventPort;
    private final RetryPolicy retryPolicy;

    @Override
    @Transactional
    public void checkWithdrawal(RequestId requestId, Count count) {
        var exchange = getExchange(requestId);
        var withdrawal = getWithdrawal(exchange, count);

        publishWithdrawalEvent(withdrawal);
        saveWithdrawal(withdrawal);
    }

    private ExchangeRequest getExchange(RequestId requestId) {
        return loadExchangeRequestCachePort.loadByRequestId(requestId)
                .orElseThrow(() -> new ExchangeRequestNotFoundException(requestId));
    }

    private Withdrawal getWithdrawal(ExchangeRequest exchange, Count count) {
        try {
            return loadWithdrawalPort.loadWithdrawal(exchange);

        } catch (Exception e) {
            exchange.handleWithdrawalCheckUnknown(count, retryPolicy);
            publishEventPort.publish(exchange);
            throw e;
        }
    }

    private void publishWithdrawalEvent(Withdrawal withdrawal) {
        withdrawal.processCheckResult();
        publishEventPort.publish(withdrawal);
    }

    private void saveWithdrawal(Withdrawal withdrawal) {
        if (withdrawal.isSuccess()) {
            saveWithdrawalPort.saveWithdrawal(withdrawal);
        }
    }

}