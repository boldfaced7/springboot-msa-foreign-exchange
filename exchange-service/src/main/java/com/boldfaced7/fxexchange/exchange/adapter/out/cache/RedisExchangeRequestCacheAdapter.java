package com.boldfaced7.fxexchange.exchange.adapter.out.cache;

import com.boldfaced7.fxexchange.exchange.application.port.out.cache.DeleteExchangeRequestCachePort;
import com.boldfaced7.fxexchange.exchange.application.port.out.cache.LoadExchangeRequestCachePort;
import com.boldfaced7.fxexchange.exchange.application.port.out.cache.SaveExchangeRequestCachePort;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.RequestId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisExchangeRequestCacheAdapter implements
        LoadExchangeRequestCachePort,
        SaveExchangeRequestCachePort,
        DeleteExchangeRequestCachePort
{
    private final RedisExchangeRequestRepository exchangeRequestRepository;
    private final RedisExchangeIdIndexRepository exchangeIdIndexRepository;

    @Override
    public void deleteByRequestId(RequestId requestId) {
        if (requestId == null) return;

        exchangeRequestRepository.findById(requestId.toString())
                .ifPresent(exchangeRequest -> {
                        exchangeRequestRepository.delete(exchangeRequest);
                        exchangeIdIndexRepository.delete(exchangeRequest.getExchangeId());
                });
    }

    @Override
    public Optional<ExchangeRequest> loadByRequestId(RequestId requestId) {
        return exchangeRequestRepository.findById(requestId.value().toString())
                .flatMap(RedisExchangeRequestMapper::toDomain);
    }

    @Override
    public Optional<ExchangeRequest> loadByExchangeId(ExchangeId exchangeId) {
        return exchangeIdIndexRepository.find(exchangeId.value())
                .flatMap(exchangeRequestRepository::findById)
                .flatMap(RedisExchangeRequestMapper::toDomain);
    }

    @Override
    public void save(ExchangeRequest exchangeRequest) {
        RedisExchangeRequestMapper.toRedis(exchangeRequest)
                .map(exchangeRequestRepository::save)
                .ifPresent(exchangeIdIndexRepository::save);
        log.info("ExchangeRequest cached: {}", exchangeRequest);
    }
}