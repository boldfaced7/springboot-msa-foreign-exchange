package com.boldfaced7.fxexchange.exchange.application.service.sell.withdrawal;

import com.boldfaced7.fxexchange.exchange.application.port.out.sell.SendFxWithdrawalCheckRequestPort;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class DelayFxWithdrawalCheckServiceImpl implements DelayFxWithdrawalCheckService {

    private final static Duration DELAY_SECOND = Duration.ofSeconds(30);

    private final SendFxWithdrawalCheckRequestPort sendFxWithdrawalCheckRequestPort;

    @Override
    public void delayFxWithdrawalCheck(RequestId requestId, Count count) {
        Duration delay = DELAY_SECOND.multipliedBy(count.value() + 1);
        sendFxWithdrawalCheckRequestPort.sendFxWithdrawalCheckRequest(requestId, delay);
    }
}
