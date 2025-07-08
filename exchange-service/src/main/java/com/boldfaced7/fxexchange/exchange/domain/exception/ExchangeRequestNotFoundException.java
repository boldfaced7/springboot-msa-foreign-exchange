package com.boldfaced7.fxexchange.exchange.domain.exception;

import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

public class ExchangeRequestNotFoundException extends RuntimeException {
    public ExchangeRequestNotFoundException(RequestId requestId) {
        super("환전 요청을 찾을 수 없습니다. " + requestId);
    }

    public ExchangeRequestNotFoundException(ExchangeId exchangeId) {
        super("환전 요청을 찾을 수 없습니다. " + exchangeId);
    }

}
