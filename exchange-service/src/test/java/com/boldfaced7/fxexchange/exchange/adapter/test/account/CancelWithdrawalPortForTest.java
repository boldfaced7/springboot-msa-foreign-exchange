package com.boldfaced7.fxexchange.exchange.adapter.test.account;

import com.boldfaced7.fxexchange.exchange.adapter.test.TestUtil;
import com.boldfaced7.fxexchange.exchange.application.port.out.CancelWithdrawalPort;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Profile;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@TestConfiguration
@Profile("application-test")
public class CancelWithdrawalPortForTest implements CancelWithdrawalPort {
    private final Map<ExchangeId, TestBehavior> behaviors = new ConcurrentHashMap<>();
    private final Map<ExchangeId, Runnable> callbacks = new ConcurrentHashMap<>();

    public enum TestBehavior {
        NORMAL,
        DELAY
    }
    
    @Override
    public void cancelWithdrawal(ExchangeId exchangeId, Direction direction) {
        TestBehavior behavior = behaviors.getOrDefault(exchangeId, TestBehavior.NORMAL);
        Runnable callback = callbacks.get(exchangeId);

        log.info("[CancelWithdrawalPort] behavior: {}", behavior);

        switch (behavior) {
            case NORMAL -> TestUtil.doAsync(callback);
            case DELAY -> TestUtil.doAsyncWithDelay(callback);
        }
    }
    
    public void setBehavior(ExchangeId exchangeId, TestBehavior behavior) {
        behaviors.put(exchangeId, behavior);
    }
    public void setCallback(ExchangeId exchangeId, Runnable callback) {
        callbacks.put(exchangeId, callback);
    }


    public void reset() {
        behaviors.clear();
        callbacks.clear();
    }

    public void setNormal(ExchangeId exchangeId, Runnable callback) {
        setBehavior(exchangeId, TestBehavior.NORMAL);
        setCallback(exchangeId, callback);
    }

    public void setDelay(ExchangeId exchangeId, Runnable callback) {
        setBehavior(exchangeId, TestBehavior.DELAY);
        setCallback(exchangeId, callback);
    }
}