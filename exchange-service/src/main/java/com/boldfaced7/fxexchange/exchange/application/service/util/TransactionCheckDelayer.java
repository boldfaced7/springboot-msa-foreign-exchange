package com.boldfaced7.fxexchange.exchange.application.service.util;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.enums.TransactionCheckType;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;

public interface TransactionCheckDelayer {
    void delayTransactionCheck(
            ExchangeId exchangeId,
            Count count,
            Direction direction,
            TransactionCheckType transactionCheckType
    );
}
