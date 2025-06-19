package com.boldfaced7.fxexchange.exchange.adapter.test.account;

import com.boldfaced7.fxexchange.exchange.adapter.test.TestUtil;
import com.boldfaced7.fxexchange.exchange.application.port.out.RequestDepositPort;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.AccountCommandStatus;
import com.boldfaced7.fxexchange.exchange.domain.vo.DepositId;
import com.boldfaced7.fxexchange.exchange.domain.vo.DepositResult;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Profile;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Slf4j
@TestConfiguration
@Profile("application-test")
public class RequestDepositPortForTest implements RequestDepositPort {
    private final Map<ExchangeId, DepositResult> depositResults = new ConcurrentHashMap<>();
    private final Map<ExchangeId, TestBehavior> behaviors = new ConcurrentHashMap<>();

    public enum TestBehavior {
        SUCCESS,           // 성공 응답
        THROW_EXCEPTION,   // 예외 발생
        DELAY,             // 지연 응답
        FAILED             // 실패 응답
    }

    @Override
    public DepositResult deposit(ExchangeRequest requested) {
        ExchangeId exchangeId = requested.getExchangeId();
        TestBehavior behavior = behaviors.getOrDefault(exchangeId, TestBehavior.SUCCESS);
        Supplier<DepositResult> depositResultSupplier = () ->
                depositResults.getOrDefault(exchangeId, null);

        log.info("[RequestDepositPort] TestBehavior: {}", behavior);

        return switch (behavior) {
            case SUCCESS, FAILED -> depositResultSupplier.get();
            case THROW_EXCEPTION -> throw new RuntimeException("테스트용 예외 발생");
            case DELAY -> TestUtil.doWithDelay(depositResultSupplier);
        };
    }

    public void setDepositResult(ExchangeId exchangeId, DepositResult result) {
        depositResults.put(exchangeId, result);
    }
    
    public void setBehavior(ExchangeId exchangeId, TestBehavior behavior) {
        behaviors.put(exchangeId, behavior);
    }
    
    public void reset() {
        depositResults.clear();
        behaviors.clear();
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
        setBehavior(exchangeId, TestBehavior.FAILED);
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
    }

}