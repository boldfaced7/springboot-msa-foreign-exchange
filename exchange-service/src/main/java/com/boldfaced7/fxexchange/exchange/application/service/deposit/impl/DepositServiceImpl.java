package com.boldfaced7.fxexchange.exchange.application.service.deposit.impl;

import com.boldfaced7.fxexchange.exchange.application.service.deposit.DepositRequester;
import com.boldfaced7.fxexchange.exchange.application.service.deposit.DepositService;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestUpdater;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.DepositDetail;
import com.boldfaced7.fxexchange.exchange.domain.vo.DepositResult;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DepositServiceImpl implements DepositService {

    private final DepositRequester depositRequester;
    private final ExchangeRequestUpdater exchangeRequestUpdater;

    @Override
    @Transactional
    public DepositDetail deposit(ExchangeRequest requested) {
        var deposited = depositRequester.requestDeposit(
                requested,
                ExchangeRequest::depositSucceeded,    // 입금 성공 시, '입금 성공' 이벤트 발행
                ExchangeRequest::depositFailed,       // 입금 실패 시, '입금 실패' 이벤트 발행
                ExchangeRequest::depositResultUnknown // 예외 발생 시 '입금 결과 알 수 없음' 이벤트 발행
        );
        validateDepositResult(deposited);

        var updated = exchangeRequestUpdater.update(
                requested,
                ExchangeRequest::addDepositId,
                deposited.depositId()
        );
        return new DepositDetail(updated, deposited);
    }

    private void validateDepositResult(DepositResult deposited) {
        if (!deposited.isSuccess()) {
            throw new RuntimeException("Deposit failed: " + deposited.status().value());
        }
    }

}
