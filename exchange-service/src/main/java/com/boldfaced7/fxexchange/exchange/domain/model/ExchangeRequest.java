package com.boldfaced7.fxexchange.exchange.domain.model;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.event.deposit.*;
import com.boldfaced7.fxexchange.exchange.domain.event.exchange.*;
import com.boldfaced7.fxexchange.exchange.domain.event.withdrawal.*;
import com.boldfaced7.fxexchange.exchange.domain.vo.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExchangeRequest extends EventDomain {
    private RequestId requestId;
    private ExchangeId exchangeId;                 // 외부 추적 가능한 고유 ID
    private UserId userId;
    
    private Direction direction;                   // BUY: 원화 → 외화, SELL: 외화 → 원화
    private BaseCurrency baseCurrency;             // 외화
    private QuoteCurrency quoteCurrency;           // 원화
    
    private BaseAmount baseAmount;                 // 외화 금액
    private QuoteAmount quoteAmount;               // 원화 금액
    private ExchangeRate exchangeRate;             // 적용된 환율
    
    private ExchangeFinished exchangeFinished;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ExchangeRequest of(
        ExchangeId exchangeId,
        UserId userId,
        Direction direction,
        BaseCurrency baseCurrency,
        QuoteCurrency quoteCurrency,
        BaseAmount baseAmount,
        QuoteAmount quoteAmount,
        ExchangeRate exchangeRate
    ) {
        return new ExchangeRequest(
                null,
                exchangeId,
                userId,
                direction,
                baseCurrency,
                quoteCurrency,
                baseAmount,
                quoteAmount,
                exchangeRate,
                ExchangeFinished.NOT_FINISHED,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    public static ExchangeRequest of(
        RequestId requestId,
        ExchangeId exchangeId,
        UserId userId,
        Direction direction,
        BaseCurrency baseCurrency,
        QuoteCurrency quoteCurrency,
        BaseAmount baseAmount,
        QuoteAmount quoteAmount,
        ExchangeRate exchangeRate,
        ExchangeFinished exchangeFinished,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ExchangeRequest(
                requestId,
                exchangeId,
                userId,
                direction,
                baseCurrency,
                quoteCurrency,
                baseAmount,
                quoteAmount,
                exchangeRate,
                exchangeFinished,
                createdAt,
                updatedAt
        );
    }

    public boolean isFinished() {
        return exchangeFinished.value();
    }

    public void completeExchange(boolean isSucceeded) {
        this.exchangeFinished = new ExchangeFinished(true);

        if (isSucceeded) {
            this.markExchangeCurrencySucceeded();
        } else {
            this.markExchangeCurrencyFailed();
        }
    }

    public void handleDepositCheckUnknown(Count current, RetryPolicy retryPolicy) {
        if (current.isSmallerThan(retryPolicy.criteria())) {
            markDepositCheckUnknown(current.increase(), retryPolicy.calculateDelay(current));
        } else {
            markDepositAttemptExhausted();
        }
    }

    public void handleWithdrawalCheckUnknown(Count current, RetryPolicy retryPolicy) {
        if (current.isSmallerThan(retryPolicy.criteria())) {
            markWithdrawalCheckUnknown(current.increase(), retryPolicy.calculateDelay(current));
        } else {
            markWithdrawalAttemptExhausted();
        }
    }

    /* 환전 관련 이벤트 생성 */

    // 환전 시작
    public void markExchangeCurrencyStarted() {
        addEvent(new ExchangeCurrencyStarted(requestId, exchangeId, direction));
    }

    // 환전 성공
    private void markExchangeCurrencySucceeded() {
        addEvent(new ExchangeCurrencySucceeded(requestId, exchangeId, direction));
    }

    // 환전 실패
    private void markExchangeCurrencyFailed() {
        addEvent(new ExchangeCurrencyFailed(requestId, exchangeId, direction));
    }

    /* 입금 관련 이벤트 생성 */
    // 입금: 결과 알 수 없음

    public void markDepositUnknown(Count count) {
        addEvent(new DepositUnknown(requestId, exchangeId, direction, count, Duration.ZERO));
    }

    private void markDepositCheckUnknown(Count count, Duration delay) {
        addEvent(new DepositCheckUnknown(requestId, exchangeId, direction, count, delay));
    }

    // 입금: 결과 확인 실패 횟수 초과
    private void markDepositAttemptExhausted() {
        addEvent(new DepositAttemptExhausted(requestId, exchangeId, direction));
    }
    
    /* 출금 관련 이벤트 생성 */
    // 출금: 결과 알 수 없음
    public void markWithdrawalUnknown(Count count) {
        addEvent(new WithdrawalUnknown(requestId, exchangeId, direction, count, Duration.ZERO));
    }

    private void markWithdrawalCheckUnknown(Count count, Duration delay) {
        addEvent(new WithdrawalCheckUnknown(requestId, exchangeId, direction, count, delay));
    }

    // 출금: 결과 확인 실패 횟수 초과
    private void markWithdrawalAttemptExhausted() {
        addEvent(new WithdrawalAttemptExhausted(requestId, exchangeId, direction));
    }

}
