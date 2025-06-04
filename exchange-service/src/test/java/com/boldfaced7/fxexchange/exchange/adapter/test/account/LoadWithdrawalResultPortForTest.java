package com.boldfaced7.fxexchange.exchange.adapter.test.account;

import com.boldfaced7.fxexchange.exchange.adapter.test.TestUtil;
import com.boldfaced7.fxexchange.exchange.application.port.out.LoadWithdrawalResultPort;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.vo.AccountCommandStatus;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.WithdrawalId;
import com.boldfaced7.fxexchange.exchange.domain.vo.WithdrawalResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Slf4j
@Profile("test")
public class LoadWithdrawalResultPortForTest implements LoadWithdrawalResultPort {
    private final Map<ExchangeId, List<WithdrawalResult>> withdrawalResults = new ConcurrentHashMap<>();
    private final Map<ExchangeId, List<TestBehavior>> behaviors = new ConcurrentHashMap<>();
    private final Map<ExchangeId, Integer> callCounts = new ConcurrentHashMap<>();
    
    public enum TestBehavior {
        SUCCESS,           // 성공 응답
        THROW_EXCEPTION,  // 예외 발생
        DELAY,            // 지연 응답
        NOT_FOUND         // 데이터 없음
    }
    
    @Override
    public WithdrawalResult loadWithdrawalResult(ExchangeId exchangeId) {
        TestBehavior behavior = getBehavior(exchangeId);
        WithdrawalResult result = getWithdrawalResult(exchangeId);
        Supplier<WithdrawalResult> withdrawalResultSupplier = () -> result;

        log.info("[LoadWithdrawalResultPort] TestBehavior: {}", behavior);

        return switch (behavior) {
            case SUCCESS, NOT_FOUND -> withdrawalResultSupplier.get();
            case THROW_EXCEPTION -> throw new RuntimeException("테스트용 예외 발생");
            case DELAY -> TestUtil.doWithDelay(withdrawalResultSupplier);
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

    private WithdrawalResult getWithdrawalResult(ExchangeId exchangeId) {
        int count = callCounts.get(exchangeId) - 1; // 0-based
        List<WithdrawalResult> resultList = withdrawalResults.get(exchangeId);

        if (resultList == null || resultList.isEmpty()) {
            return null;
        }
        return (count < resultList.size())
                ? resultList.get(count)
                : resultList.getLast();
    }

    public void setWithdrawalResult(ExchangeId exchangeId, WithdrawalResult result) {
        withdrawalResults.computeIfAbsent(exchangeId, k -> new ArrayList<>())
                .add(result);
    }

    public void setWithdrawalResults(ExchangeId exchangeId, List<WithdrawalResult> results) {
        withdrawalResults.put(exchangeId, results);
    }
    
    public void setBehavior(ExchangeId exchangeId, TestBehavior behavior) {
        behaviors.computeIfAbsent(exchangeId, k -> new ArrayList<>())
                .add(behavior);
    }

    public void setBehaviors(ExchangeId exchangeId, List<TestBehavior> behaviorList) {
        behaviors.put(exchangeId, behaviorList);
    }
    
    public void reset() {
        withdrawalResults.clear();
        behaviors.clear();
        callCounts.clear();
    }

    public void setSuccess(ExchangeId exchangeId, String state, String withdrawalId) {
        var result = new WithdrawalResult(
                true,
                new AccountCommandStatus(state),
                new WithdrawalId(withdrawalId)
        );
        setBehavior(exchangeId, TestBehavior.SUCCESS);
        setWithdrawalResult(exchangeId, result);
    }

    public void setFailed(ExchangeId exchangeId, String state) {
        var result = new WithdrawalResult(
                false,
                new AccountCommandStatus(state),
                null
        );
        setBehavior(exchangeId, TestBehavior.NOT_FOUND);
        setWithdrawalResult(exchangeId, result);
    }

    public void setDelay(ExchangeId exchangeId, String state, String withdrawalId) {
        var result = new WithdrawalResult(
                true,
                new AccountCommandStatus(state),
                new WithdrawalId(withdrawalId)
        );
        setBehavior(exchangeId, TestBehavior.DELAY);
        setWithdrawalResult(exchangeId, result);
    }

    public void setThrowException(ExchangeId exchangeId) {
        setBehavior(exchangeId, TestBehavior.THROW_EXCEPTION);
        setWithdrawalResult(exchangeId, null);
    }


    @Override
    public Direction direction() {
        return Direction.BUY;
    }

}