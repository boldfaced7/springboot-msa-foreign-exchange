package com.boldfaced7.fxexchange.exchange.application.service.withdrawal.impl;

import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckWithdrawalServiceImpl implements CheckWithdrawalService {

    @Value("${exchange.withdrawal.check.max-count:3}")
    private int maxCount = 3;

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
                ExchangeRequest::withdrawalFailureChecked,    // 출금 실패 확인 시, '출금 실패 조회 완료' 이벤트 발행
                whenExceptionOccurred(count),
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

    private ExchangeEventPublisher.ParamEventPublisher<Count> whenExceptionOccurred(Count count) {
        return (count.isSmallerThan(maxCount))
                // 입금 결과 조회 실패 시, '출금 요청 확인 지연 필요' 이벤트 발행
                ? ExchangeRequest::delayingWithdrawalCheckRequired
                // 입금 결과 조회 재시도 횟수 초과 시, '경고 메시지 발송 필요' 이벤트 발행
                : ExchangeRequest::sendingWarningMessageRequired;
    }

}
