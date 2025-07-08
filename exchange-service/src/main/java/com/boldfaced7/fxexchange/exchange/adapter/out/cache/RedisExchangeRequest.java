package com.boldfaced7.fxexchange.exchange.adapter.out.cache;

import com.boldfaced7.fxexchange.exchange.domain.enums.CurrencyCode;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "exchange-request", timeToLive = 3600)
public class RedisExchangeRequest implements Serializable {

    @Id
    private String exchangeRequestId;
    private String exchangeId;
    private String userId;

    private Direction direction;
    private CurrencyCode baseCurrency;
    private CurrencyCode quoteCurrency;

    private int baseAmount;
    private int quoteAmount;
    private double exchangeRate;

    private boolean finished;
    private String createdAt;
    private String updatedAt;
}
