package com.boldfaced7.fxexchange.exchange.application.port.out.cache;

import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.RequestId;

public interface DeleteExchangeRequestCachePort {
    void deleteByRequestId(RequestId requestId);
}
