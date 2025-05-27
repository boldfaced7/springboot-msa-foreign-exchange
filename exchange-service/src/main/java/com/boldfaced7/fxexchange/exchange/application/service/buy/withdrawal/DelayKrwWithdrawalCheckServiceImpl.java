package com.boldfaced7.fxexchange.exchange.application.service.buy.withdrawal;

import com.boldfaced7.fxexchange.exchange.application.port.out.buy.SendKrwWithdrawalCheckRequestPort;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class DelayKrwWithdrawalCheckServiceImpl implements DelayKrwWithdrawalCheckService {

    private final static Duration DELAY_SECOND = Duration.ofSeconds(30);

    private final SendKrwWithdrawalCheckRequestPort sendKrwWithdrawalCheckRequestPort;

    @Override
    public void delayKrwWithdrawalCheck(RequestId requestId, Count count) {
        Duration delay = DELAY_SECOND.multipliedBy(count.value() + 1);
        sendKrwWithdrawalCheckRequestPort.sendKrwWithdrawalCheckRequest(requestId, delay);
    }
}
