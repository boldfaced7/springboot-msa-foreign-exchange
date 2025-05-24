package com.boldfaced7.fxexchange.exchange.domain.model;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.event.deposit.*;
import com.boldfaced7.fxexchange.exchange.domain.event.exchange.ExchangeCompleted;
import com.boldfaced7.fxexchange.exchange.domain.event.exchange.ExchangeFailed;
import com.boldfaced7.fxexchange.exchange.domain.event.exchange.ExchangeStarted;
import com.boldfaced7.fxexchange.exchange.domain.event.withdrawal.*;
import com.boldfaced7.fxexchange.exchange.domain.vo.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExchangeRequest {

    private RequestId requestId;
    private ExchangeId exchangeId;  // 외부 추적 가능한 고유 ID
    private UserId userId;
    
    private Direction direction;  // BUY: 원화 → 외화, SELL: 외화 → 원화
    private BaseCurrency baseCurrency;  // 요청 금액 기준 통화
    private QuoteCurrency quoteCurrency; // 상대 통화
    
    private BaseAmount baseAmount;    // 기준 통화 금액
    private QuoteAmount quoteAmount;   // 환전 후 통화 금액
    private ExchangeRate exchangeRate;  // 적용된 환율
    
    private WithdrawalId withdrawalId;      // 출금 트랜잭션 ID
    private DepositId depositId;       // 입금 트랜잭션 ID
    
    private Finished finished;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private final List<Object> events = new ArrayList<>();

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
                null,
                null,
                new Finished(false),
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
        WithdrawalId withdrawalId,
        DepositId depositId,
        Finished finished,
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
                withdrawalId,
                depositId,
                finished,
                createdAt,
                updatedAt
        );
    }

    public List<Object> pullEvents() {
        var pulled = List.copyOf(events);
        events.clear();
        return pulled;
    }

    public boolean isFinished() {
        return finished.value();
    }

    public void addDepositId(DepositId depositId) {
        this.depositId = depositId;
    }

    public void addWithdrawalId(WithdrawalId withdrawalId) {
        this.withdrawalId = withdrawalId;
    }

    public void terminate() {
        this.finished = new Finished(true);
    }

    /*
        환전 관련 이벤트 생성
     */
    public void markExchangeStarted() {
        events.add(new ExchangeStarted(requestId));
    }

    public void markExchangeFailed() {
        events.add(new ExchangeFailed(requestId));
    }

    public void markExchangeCompleted() {
        events.add(new ExchangeCompleted(requestId));
    }

    /*
        출금 관련 이벤트 생성
     */
    public void markWithdrawalCheckRequired() {
        events.add(new WithdrawalCheckRequired(requestId));
    }

    public void markWithdrawalCheckWithDelayRequired() {
        events.add(new WithdrawalCheckWithDelayRequired(requestId));
    }

    public void markWithdrawalCheckWithDelayRequired(Count count) {
        events.add(new WithdrawalCheckWithDelayRequired(requestId, count));
    }

    public void markWithdrawalCompensationRequired() {
        events.add(new WithdrawalCompensationRequired(requestId));
    }

    public void markWithdrawalCompleted() {
        events.add(new WithdrawalCompleted(requestId));
    }

    public void markWithdrawalFailed() {
        events.add(new WithdrawalFailed());
    }

    /*
        입금 관련 이벤트 생성
     */
    public void markDepositCheckRequired() {
        events.add(new DepositCheckRequired(requestId));
    }

    public void markDepositCheckWithDelayRequired() {
        events.add(new DepositCheckWithDelayRequired(requestId));
    }

    public void markDepositCheckWithDelayRequired(Count count) {
        events.add(new DepositCheckWithDelayRequired(requestId, count));
    }

    public void markDepositCompensationRequired() {
        events.add(new DepositCompensationRequired(requestId));
    }

    public void markDepositCompleted() {
        events.add(new DepositCompleted(requestId));
    }

    public void markDepositFailed() {
        events.add(new DepositFailed(requestId));
    }
}
