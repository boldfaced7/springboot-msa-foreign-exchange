package com.boldfaced7.fxexchange.exchange.application.service.exchange;

import com.boldfaced7.fxexchange.exchange.application.port.out.UpdateExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestLoader;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TerminateExchangeRequestServiceImpl implements TerminateExchangeRequestService {

    private final UpdateExchangeRequestPort updateExchangeRequestPort;
    private final ExchangeRequestLoader exchangeRequestLoader;

    @Override
    public void terminateExchangeRequest(RequestId requestId) {
        var requested = exchangeRequestLoader.loadExchangeRequest(requestId);
        requested.terminate();
        updateExchangeRequestPort.update(requested);
    }
}
