package com.boldfaced7.fxexchange.exchange.application.port.in;

import com.boldfaced7.fxexchange.exchange.domain.enums.CurrencyCode;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.*;

public record ExchangeCurrencyCommand(
        ExchangeId exchangeId,
        UserId userId,
        BaseCurrency baseCurrency,
        QuoteCurrency quoteCurrency,
        Direction direction,
        BaseAmount baseAmount,
        QuoteAmount quoteAmount,
        ExchangeRate exchangeRate
) {
        public ExchangeCurrencyCommand(
                ExchangeId exchangeId,
                UserId userId,
                BaseCurrency baseCurrency,
                BaseAmount baseAmount,
                QuoteAmount quoteAmount,
                Direction direction,
                ExchangeRate exchangeRate
        ) {
                this(
                        exchangeId,
                        userId,
                        baseCurrency,
                        new QuoteCurrency(CurrencyCode.KRW),
                        direction,
                        baseAmount,
                        quoteAmount,
                        exchangeRate
                );
        }

}
