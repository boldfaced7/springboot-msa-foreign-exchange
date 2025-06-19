package com.boldfaced7.fxexchange.exchange.application.service.deposit.impl;

import com.boldfaced7.fxexchange.exchange.application.service.deposit.CheckDepositService;
import com.boldfaced7.fxexchange.exchange.application.service.deposit.DepositResultChecker;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestLoader;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestUpdater;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckDepositServiceImpl implements CheckDepositService {

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
                ExchangeRequest::depositCheckUnknown,   // 입금 결과 확인 실패 시, '입금 확인 결과 알 수 없음' 이벤트 발행
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

}
