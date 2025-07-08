package com.boldfaced7.fxexchange.exchange.adapter.out.cache;

import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.*;

import java.time.LocalDateTime;
import java.util.Optional;

public class RedisExchangeRequestMapper {

    public static Optional<ExchangeRequest> toDomain(RedisExchangeRequest redis) {
        if (redis == null) return Optional.empty();

        var converted = ExchangeRequest.of(
                new RequestId(Long.valueOf(redis.getExchangeRequestId())),
                new ExchangeId(redis.getExchangeId()),
                new UserId(redis.getUserId()),

                redis.getDirection(),
                new BaseCurrency(redis.getBaseCurrency()),
                new QuoteCurrency(redis.getQuoteCurrency()),

                new BaseAmount(redis.getBaseAmount()),
                new QuoteAmount(redis.getQuoteAmount()),
                new ExchangeRate(redis.getExchangeRate()),

                new ExchangeFinished(redis.isFinished()),
                LocalDateTime.parse(redis.getCreatedAt()),
                LocalDateTime.parse(redis.getUpdatedAt())
        );

        return Optional.of(converted);
    }

    public static Optional<RedisExchangeRequest> toRedis(ExchangeRequest domain) {
        if (domain == null) return Optional.empty();

        var converted = new RedisExchangeRequest(
                (domain.getRequestId() == null) ? null : domain.getRequestId().toString(),
                domain.getExchangeId().value(),
                domain.getUserId().value(),

                domain.getDirection(),
                domain.getBaseCurrency().value(),
                domain.getQuoteCurrency().value(),

                domain.getBaseAmount().value(),
                domain.getQuoteAmount().value(),
                domain.getExchangeRate().value(),

                domain.isFinished(),
                domain.getCreatedAt().toString(),
                domain.getUpdatedAt().toString()
        );

        return Optional.of(converted);
    }
}
