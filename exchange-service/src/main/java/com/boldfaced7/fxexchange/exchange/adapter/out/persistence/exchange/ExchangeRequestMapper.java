package com.boldfaced7.fxexchange.exchange.adapter.out.persistence.exchange;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.*;

public class ExchangeRequestMapper {
    public static ExchangeRequest toDomain(ExchangeRequestJpa jpa) {
        return ExchangeRequest.of(
                new RequestId(jpa.getExchangeRequestId()),
                new ExchangeId(jpa.getExchangeId()),
                new UserId(jpa.getUserId()),

                jpa.getDirection(),
                new BaseCurrency(jpa.getBaseCurrency()),
                new QuoteCurrency(jpa.getQuoteCurrency()),

                new BaseAmount(jpa.getBaseAmount()),
                new QuoteAmount(jpa.getQuoteAmount()),
                new ExchangeRate(jpa.getExchangeRate()),

                new ExchangeFinished(jpa.isFinished()),
                jpa.getCreatedAt(),
                jpa.getUpdatedAt()
        );
    }

    public static ExchangeRequestJpa toJpa(ExchangeRequest domain) {
        return new ExchangeRequestJpa(
                (domain.getRequestId() == null) ? null : domain.getRequestId().value(),
                domain.getExchangeId().value(),
                domain.getUserId().value(),

                domain.getDirection(),
                domain.getBaseCurrency().value(),
                domain.getQuoteCurrency().value(),

                domain.getBaseAmount().value(),
                domain.getQuoteAmount().value(),
                domain.getExchangeRate().value(),

                domain.isFinished(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
