package com.boldfaced7.fxexchange.exchange.application.port.in.sell;

import com.boldfaced7.fxexchange.exchange.domain.enums.CurrencyCode;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.vo.*;

public record SellForeignCurrencyCommand(
        ExchangeId exchangeId,
        UserId userId,
        BaseCurrency baseCurrency,
        QuoteCurrency quoteCurrency,
        Direction direction,
        BaseAmount baseAmount,
        QuoteAmount quoteAmount,
        ExchangeRate exchangeRate
) {

        public SellForeignCurrencyCommand(
                UserId userId,
                BaseCurrency baseCurrency,
                BaseAmount baseAmount,
                QuoteAmount quoteAmount,
                ExchangeRate exchangeRate
        ) {
                this(
                        new ExchangeId(),
                        userId,
                        baseCurrency,
                        new QuoteCurrency(CurrencyCode.KRW),
                        Direction.SELL,
                        baseAmount,
                        quoteAmount,
                        exchangeRate
                );
        }
}
