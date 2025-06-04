package com.boldfaced7.fxexchange.exchange.application.service.withdrawal.impl;

import com.boldfaced7.fxexchange.exchange.application.port.out.SendWithdrawalCheckRequestPort;
import com.boldfaced7.fxexchange.exchange.application.service.withdrawal.DelayWithdrawalCheckService;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DelayWithdrawalCheckServiceImpl implements DelayWithdrawalCheckService {

    private final static Duration DELAY_SECOND = Duration.ofSeconds(30);

    private final Map<Direction, SendWithdrawalCheckRequestPort> sendWithdrawalCheckRequestPorts;

    @Override
    public void delayWithdrawalCheck(ExchangeId exchangeId, Count count, Direction direction) {
        Duration delay = DELAY_SECOND.multipliedBy(count.value() + 1);
        sendWithdrawalCheckRequestPorts.get(direction).sendWithdrawalCheckRequest(exchangeId, delay);
    }
}
