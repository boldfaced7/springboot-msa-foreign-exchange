package com.boldfaced7.fxexchange.exchange.application.service.sell.deposit;

import com.boldfaced7.fxexchange.exchange.application.port.out.sell.SendKrwDepositCheckRequestPort;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class DelayKrwDepositCheckServiceImpl implements DelayKrwDepositCheckService {

    private final static Duration DELAY_SECOND = Duration.ofSeconds(30);

    private final SendKrwDepositCheckRequestPort sendKrwDepositCheckRequestPort;

    @Override
    public void delayKrwDepositCheck(RequestId requestId, Count count) {
        Duration delay = DELAY_SECOND.multipliedBy(count.value() + 1);
        sendKrwDepositCheckRequestPort.sendKrwDepositCheckRequest(requestId, delay);
    }
}
