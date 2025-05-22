package com.boldfaced7.fxexchange.exchange.domain.model;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.vo.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExchangeRequest {

    private ExchangeRequestId id;
    private ExchangeId exchangeId;  // 외부 추적 가능한 고유 ID
    private UserId userId;
    
    private Direction direction;  // BUY: 원화 → 외화, SELL: 외화 → 원화
    private BaseCurrency baseCurrency;  // 요청 금액 기준 통화
    private QuoteCurrency quoteCurrency; // 상대 통화
    
    private BaseAmount baseAmount;    // 기준 통화 금액
    private QuoteAmount quoteAmount;   // 환전 후 통화 금액
    private ExchangeRate exchangeRate;  // 적용된 환율
    
    private WithdrawId withdrawId;      // 출금 트랜잭션 ID
    private DepositId depositId;       // 입금 트랜잭션 ID
    
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
                null, 
                null, 
                LocalDateTime.now(), 
                LocalDateTime.now()
        );
    }

    public static ExchangeRequest of(
        ExchangeRequestId id,
        ExchangeId exchangeId,
        UserId userId,
        Direction direction,
        BaseCurrency baseCurrency,
        QuoteCurrency quoteCurrency,
        BaseAmount baseAmount,
        QuoteAmount quoteAmount,
        ExchangeRate exchangeRate,
        WithdrawId withdrawId,
        DepositId depositId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ExchangeRequest(
                id,
                exchangeId,
                userId,
                direction,
                baseCurrency,
                quoteCurrency,
                baseAmount,
                quoteAmount,
                exchangeRate,
                withdrawId,
                depositId,
                createdAt,
                updatedAt
        );
    }
        



    public void registerWithdrawId(WithdrawId withdrawId) {
        this.withdrawId = withdrawId;
    }

    public void registerDepositId(DepositId depositId) {
        if (withdrawId == null) {
            throw new IllegalStateException("출금 트랜잭션 ID가 없습니다.");
        }
        this.depositId = depositId;
    }


}
