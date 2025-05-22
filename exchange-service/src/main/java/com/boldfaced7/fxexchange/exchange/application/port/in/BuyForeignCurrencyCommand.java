package com.boldfaced7.fxexchange.exchange.application.port.in;

import com.boldfaced7.fxexchange.exchange.domain.enums.CurrencyCode;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.vo.*;

public record BuyForeignCurrencyCommand(
        ExchangeId exchangeId,
        UserId userId,
        BaseCurrency baseCurrency,
        QuoteCurrency quoteCurrency,
        Direction direction,
        BaseAmount baseAmount,
        QuoteAmount quoteAmount,
        ExchangeRate exchangeRate
) {

        public BuyForeignCurrencyCommand(
                UserId userId,
                QuoteCurrency quoteCurrency,
                BaseAmount baseAmount,
                QuoteAmount quoteAmount,
                ExchangeRate exchangeRate
        ) {
                this(
                        new ExchangeId(),
                        userId, 
                        new BaseCurrency(CurrencyCode.KRW), 
                        quoteCurrency, 
                        Direction.BUY, 
                        baseAmount, 
                        quoteAmount, 
                        exchangeRate
                );
        }
}
