package com.boldfaced7.fxexchange.exchange.application.service.withdrawal.impl;

import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestLoader;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestUpdater;
import com.boldfaced7.fxexchange.exchange.application.service.withdrawal.CheckWithdrawalService;
import com.boldfaced7.fxexchange.exchange.application.service.withdrawal.WithdrawalResultChecker;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckWithdrawalServiceImpl implements CheckWithdrawalService {

    private final ExchangeRequestLoader exchangeRequestLoader;
    private final WithdrawalResultChecker withdrawalResultChecker;
    private final ExchangeRequestUpdater exchangeRequestUpdater;

    @Override
    @Transactional
    public void checkWithdrawal(RequestId requestId, Count count, Direction direction) {
        var requested = exchangeRequestLoader.loadExchangeRequest(requestId);
        var withdrawn = withdrawalResultChecker.loadWithdrawalResult(
                requested,
                ExchangeRequest::withdrawalSuccessChecked, // 출금 성공 확인 시, '출금 성공 조회 완료' 이벤트 발행
                ExchangeRequest::withdrawalFailureChecked, // 출금 실패 확인 시, '출금 실패 조회 완료' 이벤트 발행
                ExchangeRequest::withdrawalCheckUnknown,   // 출금 결과 확인 실패 시, '출금 확인 결과 알 수 없음' 이벤트 발행
                count.increase()
        );
        if (withdrawn.isSuccess()) {
            exchangeRequestUpdater.update(
                    requested,
                    ExchangeRequest::addWithdrawalId,
                    withdrawn.withdrawalId()
            );
        }
    }

}
