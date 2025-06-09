package com.boldfaced7.fxexchange.exchange.application.service.withdrawal.impl;

import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestUpdater;
import com.boldfaced7.fxexchange.exchange.application.service.withdrawal.WithdrawService;
import com.boldfaced7.fxexchange.exchange.application.service.withdrawal.WithdrawalRequester;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.WithdrawalDetail;
import com.boldfaced7.fxexchange.exchange.domain.vo.WithdrawalResult;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WithdrawServiceImpl implements WithdrawService {

    private final WithdrawalRequester withdrawalRequester;
    private final ExchangeRequestUpdater exchangeRequestUpdater;

    @Override
    @Transactional
    public WithdrawalDetail withdraw(ExchangeRequest requested) {
        var withdrawn = withdrawalRequester.requestWithdrawal(
                requested,
                ExchangeRequest::withdrawalSucceeded,    // 출금 성공 시, '출금 성공' 이벤트 발행
                ExchangeRequest::withdrawalFailed,       // 출금 실패 시, '출금 실패' 이벤트 발행
                ExchangeRequest::withdrawalResultUnknown // 예외 발생 시 '출금 결과 알 수 없음' 이벤트 발행
        );
        validateWithdrawalResult(withdrawn);

        var updated = exchangeRequestUpdater.update(
                requested,
                ExchangeRequest::addWithdrawalId,
                withdrawn.withdrawalId()
        );
        return new WithdrawalDetail(updated, withdrawn);
    }

    private void validateWithdrawalResult(WithdrawalResult withdrawn) {
        if (!withdrawn.isSuccess()) {
            throw new RuntimeException("Withdraw failed: " + withdrawn.status().value());
        }
    }

}
