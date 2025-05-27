package com.boldfaced7.fxexchange.exchange.application.service.buy.deposit;

import com.boldfaced7.fxexchange.exchange.application.port.out.buy.SendFxDepositCheckRequestPort;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class DelayFxDepositCheckServiceImpl implements DelayFxDepositCheckService {

    private final static Duration DELAY_SECOND = Duration.ofSeconds(30);

    private final SendFxDepositCheckRequestPort sendFxDepositCheckRequestPort;

    @Override
    public void delayFxDepositCheck(RequestId requestId, Count count) {
        Duration delay = DELAY_SECOND.multipliedBy(count.value() + 1);
        sendFxDepositCheckRequestPort.sendFxDepositCheckRequest(requestId, delay);
    }
}
