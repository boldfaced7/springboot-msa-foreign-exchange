package com.boldfaced7.fxexchange.exchange.application.service.buy.withdrawal;

import com.boldfaced7.fxexchange.exchange.application.port.out.UpdateExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.buy.RequestKrwWithdrawalPort;
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
public class WithdrawKrwServiceImpl implements WithdrawKrwService {

    private final RequestKrwWithdrawalPort requestKrwWithdrawalPort;
    private final UpdateExchangeRequestPort updateExchangeRequestPort;
    private final ExchangeEventPublisher exchangeEventPublisher;

    @Override
    @Transactional
    public WithdrawalDetail withdrawKrw(ExchangeRequest requested) {
        var withdrawn = processWithdraw(requested);
        markEvents(withdrawn, requested);

        requested.addWithdrawalId(withdrawn.withdrawalId());
        var updated = updateExchangeRequestPort.update(requested);

        return new WithdrawalDetail(updated, withdrawn);
    }

    private WithdrawalResult processWithdraw(ExchangeRequest requested) {
        try {
            return requestKrwWithdrawalPort.withdrawKrw(requested);
        } catch (Exception e) {
            // 출금 요청 중 예외 발생 시 '출금 확인 요청' 이벤트 발행
            requested.checkingKrwWithdrawalRequired(Count.zero());
            exchangeEventPublisher.publishEvents(requested);
            throw e;
        }
    }

    private void markEvents(WithdrawalResult withdrawn, ExchangeRequest requested) {
        if (withdrawn.isSuccess()) {
            // 출금 성공 시 '출금 완료' 이벤트 발행
            requested.krwWithdrawalCompleted();
        } else {
            // 출금 실패 시 '환전(외화 구매) 실패' 이벤트 발행
            requested.buyingFailed();
        }
        exchangeEventPublisher.publishEvents(requested);
        if (!withdrawn.success()) {
            throw new RuntimeException("Withdraw failed: " + withdrawn.status().value());
        }
    }

}
