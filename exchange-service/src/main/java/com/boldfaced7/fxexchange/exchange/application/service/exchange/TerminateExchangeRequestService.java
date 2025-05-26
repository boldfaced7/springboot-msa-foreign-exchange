package com.boldfaced7.fxexchange.exchange.application.service.exchange;

import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;

public interface TerminateExchangeRequestService {
    void terminateExchangeRequest(RequestId requestId);
}
