package com.boldfaced7.fxexchange.exchange.adapter.out.persistence;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.*;

public class ExchangeRequestMapper {
    static ExchangeRequest toDomain(ExchangeRequestJpa jpa) {
        return ExchangeRequest.of(
                new ExchangeRequestId(jpa.getExchangeRequestId()),
                new ExchangeId(jpa.getExchangeId()),
                new UserId(jpa.getUserId()),

                jpa.getDirection(),
                new BaseCurrency(jpa.getBaseCurrency()),
                new QuoteCurrency(jpa.getQuoteCurrency()),

                new BaseAmount(jpa.getBaseAmount()),
                new QuoteAmount(jpa.getQuoteAmount()),
                new ExchangeRate(jpa.getExchangeRate()),

                new WithdrawId(jpa.getWithdrawId()),
                new DepositId(jpa.getDepositId()),

                jpa.getCreatedAt(),
                jpa.getUpdatedAt()
        );
    }

    static ExchangeRequestJpa toJpa(ExchangeRequest domain) {
        return new ExchangeRequestJpa(
            domain.getId().value(),
            domain.getExchangeId().value(),
            domain.getUserId().value(),

            domain.getDirection(),
            domain.getBaseCurrency().value(),
            domain.getQuoteCurrency().value(),

            domain.getBaseAmount().value(),
            domain.getQuoteAmount().value(),
            domain.getExchangeRate().value(),

            domain.getWithdrawId().value(),
            domain.getDepositId().value(),

            domain.getCreatedAt(),
            domain.getUpdatedAt()
        );
    }
}
