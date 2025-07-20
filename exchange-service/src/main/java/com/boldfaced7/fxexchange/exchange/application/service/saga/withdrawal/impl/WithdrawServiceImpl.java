package com.boldfaced7.fxexchange.exchange.application.service.saga.withdrawal.impl;

import com.boldfaced7.fxexchange.exchange.application.port.out.cache.SaveExchangeRequestCachePort;
import com.boldfaced7.fxexchange.exchange.application.port.out.event.PublishEventPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.withdrawal.RequestWithdrawalPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.withdrawal.SaveWithdrawalPort;
import com.boldfaced7.fxexchange.exchange.application.service.saga.withdrawal.WithdrawService;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.model.Withdrawal;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.withdrawal.WithdrawalDetail;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawServiceImpl implements WithdrawService {

    private final RequestWithdrawalPort requestWithdrawalPort;
    private final SaveWithdrawalPort saveWithdrawalPort;
    private final SaveExchangeRequestCachePort saveExchangeRequestCachePort;
    private final PublishEventPort publishEventPort;

    @Override
    @Transactional
    public WithdrawalDetail withdraw(ExchangeRequest exchange) {
        var withdrawal = requestWithdrawal(exchange);

        publishWithdrawalEvent(withdrawal);
        saveWithdrawal(withdrawal);

        return new WithdrawalDetail(exchange, withdrawal);
    }

    private Withdrawal requestWithdrawal(ExchangeRequest exchange) {
        try {
            return requestWithdrawalPort.withdraw(exchange);

        } catch (Exception e) {
            saveExchangeRequestCachePort.save(exchange);
            exchange.markWithdrawalUnknown(Count.zero());
            publishEventPort.publish(exchange);
            throw e;
        }
    }

    private void publishWithdrawalEvent(Withdrawal withdrawal) {
        withdrawal.processTransactionResult();
        publishEventPort.publish(withdrawal);
    }

    private void saveWithdrawal(Withdrawal withdrawal) {
        if (!withdrawal.isSuccess()) {
            throw new IllegalStateException("출금 실패");
        }
        saveWithdrawalPort.saveWithdrawal(withdrawal);

    }

}
