package com.boldfaced7.fxexchange.exchange.application.service.deposit.impl;

import com.boldfaced7.fxexchange.exchange.application.service.deposit.CheckDepositService;
import com.boldfaced7.fxexchange.exchange.application.service.deposit.DepositResultChecker;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher.ParamEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestLoader;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestUpdater;
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
public class CheckDepositServiceImpl implements CheckDepositService {

    @Value("${exchange.deposit.check.max-count:3}")
    private int maxCount = 3;

    private final ExchangeRequestLoader exchangeRequestLoader;
    private final DepositResultChecker depositResultChecker;
    private final ExchangeRequestUpdater exchangeRequestUpdater;

    @Override
    @Transactional
    public void checkDeposit(RequestId requestId, Count count, Direction direction) {
        var requested = exchangeRequestLoader.loadExchangeRequest(requestId);
        var deposited = depositResultChecker.loadDepositResult(
                requested,
                ExchangeRequest::depositSuccessChecked, // 입금 성공 확인 시, '입금 성공 조회 완료' 이벤트 발행
                ExchangeRequest::depositFailureChecked, // 입금 실패 확인 시, '입금 실패 조회 완료' 이벤트 발행
                whenExceptionOccurred(count),
                count.increase()
        );
        if (deposited.isSuccess()) {
            exchangeRequestUpdater.update(
                    requested,
                    ExchangeRequest::addDepositId,
                    deposited.depositId()
            );
        }
    }

    private ParamEventPublisher<Count> whenExceptionOccurred(Count count) {
        return (count.isSmallerThan(maxCount))
                // 입금 결과 조회 실패 시, '입금 요청 확인 지연 필요' 이벤트 발행
                ? ExchangeRequest::delayingDepositCheckRequired
                // 입금 결과 조회 재시도 횟수 초과 시, '경고 메시지 발송 필요' 이벤트 발행
                : ExchangeRequest::sendingWarningMessageRequired;
    }

}
