package com.boldfaced7.fxexchange.exchange.application.service.sell.withdrawal;

import com.boldfaced7.fxexchange.exchange.application.port.out.UpdateExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.sell.RequestFxWithdrawalPort;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.WithdrawalDetail;
import com.boldfaced7.fxexchange.exchange.domain.vo.WithdrawalResult;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WithdrawFxServiceImpl implements WithdrawFxService {

    private final RequestFxWithdrawalPort requestFxWithdrawalPort;
    private final UpdateExchangeRequestPort updateExchangeRequestPort;
    private final ExchangeEventPublisher exchangeEventPublisher;

    @Override
    @Transactional
    public WithdrawalDetail withdrawFx(ExchangeRequest requested) {
        var withdrawn = processWithdraw(requested);
        markEvents(withdrawn, requested);

        requested.addWithdrawalId(withdrawn.withdrawalId());
        var updated = updateExchangeRequestPort.update(requested);

        return new WithdrawalDetail(updated, withdrawn);
    }

    private WithdrawalResult processWithdraw(ExchangeRequest requested) {
        try {
            return requestFxWithdrawalPort.withdrawFx(requested);
        } catch (Exception e) {
            // 출금 요청 중 예외 발생 시 '출금 확인 요청' 이벤트 발행
            requested.checkingFxWithdrawalRequired(Count.zero());
            exchangeEventPublisher.publishEvents(requested);
            throw e;
        }
    }

    private void markEvents(WithdrawalResult withdrawn, ExchangeRequest requested) {
        if (withdrawn.isSuccess()) {
            // 출금 성공 시 '출금 완료' 이벤트 발행
            requested.fxWithdrawalCompleted();
        } else {
            // 출금 실패 시 '환전(외화 판매) 실패' 이벤트 발행
            requested.sellingFailed();
        }
        exchangeEventPublisher.publishEvents(requested);
        if (!withdrawn.isSuccess()) {
            throw new RuntimeException("Withdraw failed: " + withdrawn.status().value());
        }
    }

}
