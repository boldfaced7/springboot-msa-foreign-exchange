package com.boldfaced7.fxexchange.exchange.application.service.buy.deposit;

import com.boldfaced7.fxexchange.exchange.application.port.out.UpdateExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.buy.LoadFxDepositResultPort;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestLoader;
import com.boldfaced7.fxexchange.exchange.application.service.util.WarningMessageSender;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.DepositResult;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckFxDepositServiceImpl implements CheckFxDepositService {

    private final static int MAX_COUNT = 3;

    private final LoadFxDepositResultPort loadFxDepositResultPort;
    private final UpdateExchangeRequestPort updateExchangeRequestPort;
    private final ExchangeRequestLoader exchangeRequestLoader;
    private final ExchangeEventPublisher exchangeEventPublisher;
    private final WarningMessageSender warningMessageSender;

    @Override
    @Transactional
    public void checkFxDeposit(RequestId requestId, Count count) {
        var requested = exchangeRequestLoader.loadExchangeRequest(requestId);
        var deposited = loadFxDepositResult(requested, count);
        markEvents(requested, deposited);

        if (deposited.isSuccess()) {
            requested.addDepositId(deposited.depositId());
            updateExchangeRequestPort.update(requested);
        }
    }

    private DepositResult loadFxDepositResult(ExchangeRequest requested, Count count) {
        try {
            return loadFxDepositResultPort.loadFxDepositResult(
                    requested.getExchangeId()
            );
        } catch (Exception e) {
            handleRetry(requested, count);
            throw e;
        }
    }

    private void handleRetry(ExchangeRequest requested, Count count) {
        if (count.isSmallerThan(MAX_COUNT)) {
            // 입금 결과 조회 실패 시, '입금 요청 지연 확인 필요' 이벤트 발행
            requested.delayingFxDepositCheckRequired(count.increase());
            exchangeEventPublisher.publishEvents(requested);
        } else {
            // 입금 결과 조회 재시도 횟수 초과 시, 경고 메시지 발송
            warningMessageSender.sendWarningMessage(
                    requested.getRequestId(),
                    requested.getExchangeId()
            );
        }
    }

    private void markEvents(ExchangeRequest requested, DepositResult deposited) {
        if (deposited.isSuccess()) {
            // 입금 성공 확인 시, '환전(외화 구매) 완료' 이벤트 발행
            requested.buyingCompleted();
        } else {
            // 입금 실패 확인 시, '원화 출금 취소' 이벤트 발행
            requested.cancelingKrwWithdrawalRequired();
        }
        exchangeEventPublisher.publishEvents(requested);
    }
}
