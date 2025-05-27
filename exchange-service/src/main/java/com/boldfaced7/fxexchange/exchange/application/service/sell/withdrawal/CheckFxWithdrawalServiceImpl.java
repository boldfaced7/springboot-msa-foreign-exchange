package com.boldfaced7.fxexchange.exchange.application.service.sell.withdrawal;

import com.boldfaced7.fxexchange.exchange.application.port.out.sell.LoadFxWithdrawalResultPort;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestLoader;
import com.boldfaced7.fxexchange.exchange.application.service.util.WarningMessageSender;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;
import com.boldfaced7.fxexchange.exchange.domain.vo.WithdrawalResult;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckFxWithdrawalServiceImpl implements CheckFxWithdrawalService {

    private final static int MAX_COUNT = 3;

    private final LoadFxWithdrawalResultPort loadFxWithdrawalResultPort;
    private final ExchangeRequestLoader exchangeRequestLoader;
    private final ExchangeEventPublisher exchangeEventPublisher;
    private final WarningMessageSender warningMessageSender;

    @Override
    @Transactional
    public void checkFxWithdrawal(RequestId requestId, Count count) {
        var requested = exchangeRequestLoader.loadExchangeRequest(requestId);
        var withdrawn = loadFxWithdrawalResult(requested, count);
        markEvents(requested, withdrawn);
    }

    private WithdrawalResult loadFxWithdrawalResult(ExchangeRequest requested, Count count) {
        try {
            return loadFxWithdrawalResultPort.loadFxWithdrawalResult(
                    requested.getExchangeId()
            );
        } catch (Exception e) {
            handleRetry(requested, count);
            throw e;
        }
    }

    private void handleRetry(ExchangeRequest requested, Count count) {
        if (count.isSmallerThan(MAX_COUNT)) {
            // 출금 결과 조회 실패 시, '출금 요청 지연 확인 필요' 이벤트 발행
            requested.delayingFxWithdrawalCheckRequired(count.increase());
            exchangeEventPublisher.publishEvents(requested);
        } else {
            // 출금 결과 조회 재시도 횟수 초과 시, 경고 메시지 발송
            warningMessageSender.sendWarningMessage(
                    requested.getRequestId(),
                    requested.getExchangeId()
            );
        }
    }

    private void markEvents(ExchangeRequest requested, WithdrawalResult withdrawn) {
        if (withdrawn.isSuccess()) {
            // 출금 성공 확인 시, '출금 요청 취소 필요' 이벤트 발행
            requested.cancelingFxWithdrawalRequired();
        } else {
            // 출금 실패 확인 시, '환전(외화 판매) 실패' 이벤트 발행
            requested.sellingFailed();
        }
        exchangeEventPublisher.publishEvents(requested);
    }

}
