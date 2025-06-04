package com.boldfaced7.fxexchange.exchange.adapter.test.account;

import com.boldfaced7.fxexchange.exchange.adapter.test.TestUtil;
import com.boldfaced7.fxexchange.exchange.application.port.out.SendDepositCheckRequestPort;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Profile("test")
public class SendDepositCheckRequestPortForTest implements SendDepositCheckRequestPort {
    private final Map<ExchangeId, List<TestBehavior>> behaviors = new ConcurrentHashMap<>();
    private final Map<ExchangeId, List<Runnable>> callbacks = new ConcurrentHashMap<>();
    private final Map<ExchangeId, Integer> callCounts = new ConcurrentHashMap<>();

    public enum TestBehavior {
        NORMAL,           // 정상 동작
        DELAY             // 지연 동작
    }
    
    @Override
    public void sendDepositCheckRequest(ExchangeId exchangeId, Duration delay) {
        int count = callCounts.merge(exchangeId, 1, Integer::sum) - 1; // 0-based
        List<TestBehavior> behaviorList = behaviors.get(exchangeId);
        List<Runnable> callbackList = callbacks.get(exchangeId);

        TestBehavior behavior = TestBehavior.NORMAL;
        Runnable callback = null;

        if (behaviorList != null && !behaviorList.isEmpty()) {
            behavior = (count < behaviorList.size())
                    ? behaviorList.get(count)
                    : behaviorList.getLast();
        }

        if (callbackList != null && !callbackList.isEmpty()) {
            callback = (count < callbackList.size())
                    ? callbackList.get(count)
                    : callbackList.getLast();
        }

        log.info("[SendDepositCheckRequestPort] behavior: {}", behavior);

        switch (behavior) {
            case NORMAL -> TestUtil.doAsync(callback);
            case DELAY -> TestUtil.doAsyncWithDelay(callback);
        }
    }
    
    public void setBehavior(ExchangeId exchangeId, TestBehavior behavior) {
        behaviors.computeIfAbsent(exchangeId, k -> new ArrayList<>())
                .add(behavior);
    }
    
    public void setBehaviors(ExchangeId exchangeId, List<TestBehavior> behaviorList) {
        behaviors.put(exchangeId, behaviorList);
    }
    
    public void setCallback(ExchangeId exchangeId, Runnable callback) {
        callbacks.computeIfAbsent(exchangeId, k -> new ArrayList<>())
                .add(callback);
    }
    
    public void setCallbacks(ExchangeId exchangeId, List<Runnable> callbackList) {
        callbacks.put(exchangeId, callbackList);
    }
    
    public void reset() {
        behaviors.clear();
        callbacks.clear();
        callCounts.clear();
    }

    public void setNormal(ExchangeId exchangeId, Runnable callback) {
        setBehavior(exchangeId, TestBehavior.NORMAL);
        setCallback(exchangeId, callback);
    }

    public void setDelay(ExchangeId exchangeId, Runnable callback) {
        setBehavior(exchangeId, TestBehavior.DELAY);
        setCallback(exchangeId, callback);
    }


    @Override
    public Direction direction() {
        return Direction.BUY;
    }

}