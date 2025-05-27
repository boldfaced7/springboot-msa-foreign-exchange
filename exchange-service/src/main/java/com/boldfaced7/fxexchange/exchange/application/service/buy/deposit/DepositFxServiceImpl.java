package com.boldfaced7.fxexchange.exchange.application.service.buy.deposit;

import com.boldfaced7.fxexchange.exchange.application.port.out.UpdateExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.buy.RequestFxDepositPort;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.DepositDetail;
import com.boldfaced7.fxexchange.exchange.domain.vo.DepositResult;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DepositFxServiceImpl implements DepositFxService {

    private final RequestFxDepositPort requestFxDepositPort;
    private final UpdateExchangeRequestPort updateExchangeRequestPort;
    private final ExchangeEventPublisher exchangeEventPublisher;

    @Override
    @Transactional
    public DepositDetail depositFx(ExchangeRequest requested) {
        var deposited = processDeposit(requested);
        markEvents(deposited, requested);

        requested.addDepositId(deposited.depositId());
        var updated = updateExchangeRequestPort.update(requested);

        return new DepositDetail(updated, deposited);
    }

    private DepositResult processDeposit(ExchangeRequest requested) {
        try {
            return requestFxDepositPort.depositFx(requested);
        } catch (Exception e) {
            // 입금 요청 중 예외 발생 시 '입금 확인 요청' 이벤트 발행
            requested.checkingFxDepositRequired(Count.zero());
            exchangeEventPublisher.publishEvents(requested);
            throw e;
        }
    }

    private void markEvents(DepositResult deposited, ExchangeRequest requested) {
        if (deposited.isSuccess()) {
            // 입금 성공 시, '환전(외화 구매) 완료' 이벤트 발행
            requested.buyingCompleted();
        } else {
            // 입금 실패 시, '원화 출금 취소' 이벤트 발행
            requested.cancelingKrwWithdrawalRequired();
        }
        exchangeEventPublisher.publishEvents(requested);
        if (!deposited.isSuccess()) {
            throw new RuntimeException("Deposit failed: " + deposited.status().value());
        }
    }

}
