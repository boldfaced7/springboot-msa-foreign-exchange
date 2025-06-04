package com.boldfaced7.fxexchange.exchange.application.service.deposit.impl;

import com.boldfaced7.fxexchange.exchange.application.port.out.SendDepositCheckRequestPort;
import com.boldfaced7.fxexchange.exchange.application.service.deposit.DelayDepositCheckService;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DelayDepositCheckServiceImpl implements DelayDepositCheckService {

    private final static Duration DELAY_SECOND = Duration.ofSeconds(30);

    private final Map<Direction, SendDepositCheckRequestPort> sendDepositCheckRequestPorts;

    @Override
    public void delayDepositCheck(ExchangeId exchangeId, Count count, Direction direction) {
        Duration delay = DELAY_SECOND.multipliedBy(count.value() + 1);
        sendDepositCheckRequestPorts.get(direction).sendDepositCheckRequest(exchangeId, delay);
    }
}
