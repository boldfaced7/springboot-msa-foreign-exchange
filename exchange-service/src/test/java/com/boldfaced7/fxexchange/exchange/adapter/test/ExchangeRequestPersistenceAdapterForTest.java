package com.boldfaced7.fxexchange.exchange.adapter.test;

import com.boldfaced7.fxexchange.exchange.application.port.out.LoadExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.SaveExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.UpdateExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Profile("test")
public class ExchangeRequestPersistenceAdapterForTest implements
        LoadExchangeRequestPort,
        SaveExchangeRequestPort,
        UpdateExchangeRequestPort
{
    private final AtomicLong id = new AtomicLong(1);
    private final ConcurrentHashMap<RequestId, ExchangeRequest> exchangeRequests = new ConcurrentHashMap<>();

    @Override
    public ExchangeRequest loadByRequestId(RequestId requestId) {
        return exchangeRequests.get(requestId);
    }

    @Override
    public ExchangeRequest loadByExchangeId(ExchangeId exchangeId) {
        return exchangeRequests.values().stream()
                .filter(exchangeRequest -> exchangeRequest.getExchangeId().equals(exchangeId))
                .findAny()
                .orElse(null);
    }

    @Override
    public ExchangeRequest save(ExchangeRequest exchangeRequest) {
        RequestId requestId = new RequestId(id.getAndIncrement());

        var toBeSaved = ExchangeRequest.of(
                requestId,
                exchangeRequest.getExchangeId(),
                exchangeRequest.getUserId(),
                exchangeRequest.getDirection(),
                exchangeRequest.getBaseCurrency(),
                exchangeRequest.getQuoteCurrency(),
                exchangeRequest.getBaseAmount(),
                exchangeRequest.getQuoteAmount(),
                exchangeRequest.getExchangeRate(),
                exchangeRequest.getWithdrawalId(),
                exchangeRequest.getDepositId(),
                exchangeRequest.getWithdrawalCancelId(),
                exchangeRequest.getFinished(),
                exchangeRequest.getCreatedAt(),
                exchangeRequest.getUpdatedAt()
        );
        exchangeRequests.put(requestId, toBeSaved);
        return toBeSaved;
    }

    @Override
    public ExchangeRequest update(ExchangeRequest exchangeRequest) {
        exchangeRequests.put(exchangeRequest.getRequestId(), exchangeRequest);
        return exchangeRequest;
    }

    public RequestId getRequestId() {
        return new RequestId(id.get()-1);
    }

    public List<ExchangeRequest> loadAll() {
        return exchangeRequests.values().stream().toList();
    }


    public void reset() {
        exchangeRequests.clear();
        id.set(1);
    }
}
