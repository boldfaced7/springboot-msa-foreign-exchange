package com.boldfaced7.fxexchange.exchange.adapter.test.account;

import com.boldfaced7.fxexchange.exchange.adapter.test.TestUtil;
import com.boldfaced7.fxexchange.exchange.application.port.out.RequestWithdrawalPort;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.AccountCommandStatus;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.WithdrawalId;
import com.boldfaced7.fxexchange.exchange.domain.vo.WithdrawalResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Slf4j
@Profile("test")
public class RequestWithdrawalPortForTest implements RequestWithdrawalPort {
    private final Map<ExchangeId, WithdrawalResult> withdrawalResults = new ConcurrentHashMap<>();
    private final Map<ExchangeId, TestBehavior> behaviors = new ConcurrentHashMap<>();

    public enum TestBehavior {
        SUCCESS,           // 성공 응답
        THROW_EXCEPTION,   // 예외 발생
        DELAY,             // 지연 응답
        FAILED             // 실패 응답
    }

    @Override
    public WithdrawalResult withdraw(ExchangeRequest requested) {
        ExchangeId exchangeId = requested.getExchangeId();
        TestBehavior behavior = behaviors.getOrDefault(exchangeId, TestBehavior.SUCCESS);
        Supplier<WithdrawalResult> withdrawalResultSupplier = () ->
                withdrawalResults.getOrDefault(exchangeId, null);

        log.info("[RequestWithdrawalPort] TestBehavior: {}", behavior);

        return switch (behavior) {
            case SUCCESS, FAILED -> withdrawalResultSupplier.get();
            case THROW_EXCEPTION -> throw new RuntimeException("테스트용 예외 발생");
            case DELAY -> TestUtil.doWithDelay(withdrawalResultSupplier);
        };
    }

    public void setWithdrawalResult(ExchangeId exchangeId, WithdrawalResult result) {
        withdrawalResults.put(exchangeId, result);
    }

    public void setBehavior(ExchangeId exchangeId, TestBehavior behavior) {
        behaviors.put(exchangeId, behavior);
    }

    public void reset() {
        withdrawalResults.clear();
        behaviors.clear();
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
        setBehavior(exchangeId, TestBehavior.FAILED);
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
    }


    @Override
    public Direction direction() {
        return Direction.BUY;
    }

}