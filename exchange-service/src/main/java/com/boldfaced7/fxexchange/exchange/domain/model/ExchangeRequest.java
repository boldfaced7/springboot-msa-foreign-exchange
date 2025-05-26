package com.boldfaced7.fxexchange.exchange.domain.model;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.event.buy.*;
import com.boldfaced7.fxexchange.exchange.domain.event.sell.*;
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
        환전(외화 구매) 관련 이벤트 생성
     */
    public void buyingStarted() {
        events.add(new BuyingStarted(requestId));
    }

    public void buyingFailed() {
        events.add(new BuyingFailed(requestId));
    }

    public void buyingCompleted() {
        events.add(new BuyingCompleted(requestId));
    }

    /*
        원화 출금 관련 이벤트 생성
     */
    public void checkingKrwWithdrawalRequired(Count count) {
        events.add(new CheckingKrwWithdrawalRequired(requestId, count));
    }

    public void delayingKrwWithdrawalCheckRequired(Count count) {
        events.add(new DelayingKrwWithdrawalCheckRequired(requestId, count));
    }

    public void cancelingKrwWithdrawalRequired() {
        events.add(new CancelingKrwWithdrawalRequired(exchangeId));
    }

    public void krwWithdrawalCompleted() {
        events.add(new KrwWithdrawalCompleted(requestId));
    }
    
    /*
        외화 입금 관련 이벤트 생성
     */
    public void checkingFxDepositRequired(Count count) {
        events.add(new CheckingFxDepositRequired(requestId, count));
    }

    public void delayingFxDepositCheckRequired(Count count) {
        events.add(new DelayingFxDepositCheckRequired(requestId, count));
    }

    /*
        환전(외화 판매) 관련 이벤트 생성
     */
    public void sellingStarted() {
        events.add(new SellingStarted(requestId));
    }

    public void sellingFailed() {
        events.add(new SellingFailed(requestId));
    }

    public void sellingCompleted() {
        events.add(new SellingCompleted(requestId));
    }

    /*
        외화 출금 관련 이벤트 생성
     */
    public void checkingFxWithdrawalRequired(Count count) {
        events.add(new CheckingFxWithdrawalRequired(requestId, count));
    }

    public void delayingFxWithdrawalCheckRequired(Count count) {
        events.add(new DelayingFxWithdrawalCheckRequired(requestId, count));
    }

    public void cancelingFxWithdrawalRequired() {
        events.add(new CancelingFxWithdrawalRequired(exchangeId));
    }

    public void fxWithdrawalCompleted() {
        events.add(new FxWithdrawalCompleted(requestId));
    }

    /*
        원화 입금 관련 이벤트 생성
     */
    public void checkingKrwDepositRequired(Count count) {
        events.add(new CheckingKrwDepositRequired(requestId, count));
    }

    public void delayingKrwDepositCheckRequired(Count count) {
        events.add(new DelayingKrwDepositCheckRequired(requestId, count));
    }
}
