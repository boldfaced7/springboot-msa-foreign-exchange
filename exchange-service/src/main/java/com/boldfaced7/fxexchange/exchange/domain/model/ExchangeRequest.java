package com.boldfaced7.fxexchange.exchange.domain.model;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.event.DomainEvent;
import com.boldfaced7.fxexchange.exchange.domain.event.deposit.*;
import com.boldfaced7.fxexchange.exchange.domain.event.request.ExchangeCurrencyFailed;
import com.boldfaced7.fxexchange.exchange.domain.event.request.ExchangeCurrencyStarted;
import com.boldfaced7.fxexchange.exchange.domain.event.request.ExchangeCurrencySucceeded;
import com.boldfaced7.fxexchange.exchange.domain.event.withdrawal.*;
import com.boldfaced7.fxexchange.exchange.domain.vo.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExchangeRequest {
    private RequestId requestId;
    private ExchangeId exchangeId;                 // 외부 추적 가능한 고유 ID
    private UserId userId;
    
    private Direction direction;                   // BUY: 원화 → 외화, SELL: 외화 → 원화
    private BaseCurrency baseCurrency;             // 외화
    private QuoteCurrency quoteCurrency;           // 원화
    
    private BaseAmount baseAmount;                 // 외화 금액
    private QuoteAmount quoteAmount;               // 원화 금액
    private ExchangeRate exchangeRate;             // 적용된 환율
    
    private WithdrawalId withdrawalId;             // 출금 트랜잭션 ID
    private DepositId depositId;                   // 입금 트랜잭션 ID
    private WithdrawalCancelId withdrawalCancelId; // 출금 취소 트랜잭션 ID
    
    private Finished finished;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private final List<DomainEvent> events = new ArrayList<>();

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
        WithdrawalCancelId withdrawalCancelId,
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
                withdrawalCancelId,
                finished,
                createdAt,
                updatedAt
        );
    }

    public List<DomainEvent> pullEvents() {
        var pulled = List.copyOf(events);
        events.clear();
        return pulled;
    }

    public boolean isFinished() {
        return finished.value();
    }

    public void addWithdrawalId(WithdrawalId withdrawalId) {
        this.withdrawalId = withdrawalId;
    }

    public void addDepositId(DepositId depositId) {
        this.depositId = depositId;
    }

    public void addWithdrawalCancelId(WithdrawalCancelId withdrawalCancelId) {
        this.withdrawalCancelId = withdrawalCancelId;
    }

    public void terminate() {
        this.finished = new Finished(true);
    }

    /* 환전 관련 이벤트 생성 */

    // 환전 시작
    public void exchangeCurrencyStarted() {
        events.add(new ExchangeCurrencyStarted(requestId, exchangeId, direction));
    }

    // 환전 성공
    public void exchangeCurrencySucceeded() {
        events.add(new ExchangeCurrencySucceeded(requestId, exchangeId, direction));
    }

    // 환전 실패
    public void exchangeCurrencyFailed() {
        events.add(new ExchangeCurrencyFailed(requestId, exchangeId, direction));
    }

    /* 출금 관련 이벤트 생성 */

    // 출금: 성공
    public void withdrawalSucceeded() {
        events.add(new WithdrawalSucceeded(requestId, exchangeId, direction));
    }

    // 출금: 실패
    public void withdrawalFailed() {
        events.add(new WithdrawalFailed(requestId, exchangeId, direction));
    }

    // 출금: 결과 알 수 없음
    public void withdrawalResultUnknown(Count count) {
        events.add(new WithdrawalResultUnknown(requestId, exchangeId, direction, count));
    }

    // 출금: 취소됨
    public void withdrawalCancelled() {
        events.add(new WithdrawalCancelled(requestId, exchangeId, direction));
    }

    /* 출금 확인 관련 이벤트 처리 */

    // 출금 확인: 성공 확인됨
    public void withdrawalSuccessChecked() {
        events.add(new WithdrawalSuccessChecked(requestId, exchangeId, direction));
    }

    // 출금 확인: 실패 확인됨
    public void withdrawalFailureChecked() {
        events.add(new WithdrawalFailureChecked(requestId, exchangeId, direction));
    }

    // 출금 확인: 결과 알 수 없음
    public void withdrawalCheckUnknown(Count count) {
        events.add(new WithdrawalCheckUnknown(requestId, exchangeId, direction, count));
    }

    /* 입금 관련 이벤트 생성 */

    // 입금: 성공
    public void depositSucceeded() {
        events.add(new DepositSucceeded(requestId, exchangeId, direction));
    }

    // 입금: 실패
    public void depositFailed() {
        events.add(new DepositFailed(requestId, exchangeId, direction));
    }

    // 입금: 결과 알 수 없음
    public void depositResultUnknown(Count count) {
        events.add(new DepositResultUnknown(requestId, exchangeId, direction, count));
    }

    /* 입금 확인 관련 이벤트 처리 */

    // 입금 확인: 성공 확인됨
    public void depositSuccessChecked() {
        events.add(new DepositSuccessChecked(requestId, exchangeId, direction));
    }

    // 입금 확인: 실패 확인됨
    public void depositFailureChecked() {
        events.add(new DepositFailureChecked(requestId, exchangeId, direction));
    }

    // 입금 확인: 결과 알 수 없음
    public void depositCheckUnknown(Count count) {
        events.add(new DepositCheckUnknown(requestId, exchangeId, direction, count));
    }

}
