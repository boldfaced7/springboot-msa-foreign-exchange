package com.boldfaced7.fxexchange.exchange.adapter.test.account;

import com.boldfaced7.fxexchange.exchange.adapter.test.TestUtil;
import com.boldfaced7.fxexchange.exchange.application.port.out.LoadDepositResultPort;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.vo.AccountCommandStatus;
import com.boldfaced7.fxexchange.exchange.domain.vo.DepositId;
import com.boldfaced7.fxexchange.exchange.domain.vo.DepositResult;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Slf4j
@Primary
@Profile("test")
public class LoadDepositResultPortForTest implements LoadDepositResultPort {
    private final Map<ExchangeId, List<DepositResult>> depositResults = new ConcurrentHashMap<>();
    private final Map<ExchangeId, List<TestBehavior>> behaviors = new ConcurrentHashMap<>();
    private final Map<ExchangeId, Integer> callCounts = new ConcurrentHashMap<>();
    
    public enum TestBehavior {
        SUCCESS,           // 성공 응답
        THROW_EXCEPTION,   // 예외 발생
        DELAY,             // 지연 응답
        NOT_FOUND          // 데이터 없음
    }
    
    @Override
    public DepositResult loadDepositResult(ExchangeId exchangeId) {
        TestBehavior behavior = getBehavior(exchangeId);
        DepositResult result = getDepositResult(exchangeId);
        Supplier<DepositResult> depositResultSupplier = () -> result;

        log.info("[LoadDepositResultPort] TestBehavior: {}", behavior);

        return switch (behavior) {
            case SUCCESS, NOT_FOUND -> depositResultSupplier.get();
            case THROW_EXCEPTION -> throw new RuntimeException("테스트용 예외 발생");
            case DELAY -> TestUtil.doWithDelay(depositResultSupplier);
        };
    }

    private TestBehavior getBehavior(ExchangeId exchangeId) {
        int count = callCounts.merge(exchangeId, 1, Integer::sum) - 1; // 0-based
        List<TestBehavior> behaviorList = behaviors.get(exchangeId);

        if (behaviorList == null || behaviorList.isEmpty()) {
            return TestBehavior.SUCCESS;
        }
        return (count < behaviorList.size())
                ? behaviorList.get(count)
                : behaviorList.getLast();
    }

    private DepositResult getDepositResult(ExchangeId exchangeId) {
        int count = callCounts.get(exchangeId) - 1; // 0-based
        List<DepositResult> resultList = depositResults.get(exchangeId);

        if (resultList == null || resultList.isEmpty()) {
            return null;
        }
        return (count < resultList.size())
                ? resultList.get(count)
                : resultList.getLast();
    }

    public void setDepositResult(ExchangeId exchangeId, DepositResult result) {
        depositResults.computeIfAbsent(exchangeId, k -> new ArrayList<>())
                .add(result);
    }

    public void setDepositResults(ExchangeId exchangeId, List<DepositResult> results) {
        depositResults.put(exchangeId, results);
    }

    public void setBehavior(ExchangeId exchangeId, TestBehavior behavior) {
        behaviors.computeIfAbsent(exchangeId, k -> new ArrayList<>())
                .add(behavior);
    }

    public void setBehaviors(ExchangeId exchangeId, List<TestBehavior> behaviorList) {
        behaviors.put(exchangeId, behaviorList);
    }
    
    public void reset() {
        depositResults.clear();
        behaviors.clear();
        callCounts.clear();
    }

    public void setSuccess(ExchangeId exchangeId, String state, String depositId) {
        var result = new DepositResult(
                true,
                new AccountCommandStatus(state),
                new DepositId(depositId)
        );
        setBehavior(exchangeId, TestBehavior.SUCCESS);
        setDepositResult(exchangeId, result);
    }

    public void setFailed(ExchangeId exchangeId, String state) {
        var result = new DepositResult(
                false,
                new AccountCommandStatus(state),
                null
        );
        setBehavior(exchangeId, TestBehavior.NOT_FOUND);
        setDepositResult(exchangeId, result);
    }

    public void setDelay(ExchangeId exchangeId, String state, String depositId) {
        var result = new DepositResult(
                true,
                new AccountCommandStatus(state),
                new DepositId(depositId)
        );
        setBehavior(exchangeId, TestBehavior.DELAY);
        setDepositResult(exchangeId, result);
    }

    public void setThrowException(ExchangeId exchangeId) {
        setBehavior(exchangeId, TestBehavior.THROW_EXCEPTION);
        setDepositResult(exchangeId, null);
    }

    @Override
    public Direction direction() {
        return Direction.BUY;
    }

}