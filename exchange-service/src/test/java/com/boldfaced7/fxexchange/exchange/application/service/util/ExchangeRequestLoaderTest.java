package com.boldfaced7.fxexchange.exchange.application.service.util;

import com.boldfaced7.fxexchange.exchange.application.port.out.LoadExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.service.util.impl.ExchangeRequestLoaderImpl;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRequestLoaderTest {

    @InjectMocks
    private ExchangeRequestLoaderImpl exchangeRequestLoader;

    @Mock
    private LoadExchangeRequestPort loadExchangeRequestPort;

    @Mock
    private ExchangeRequest exchangeRequest;

    private RequestId requestId;
    private ExchangeId exchangeId;

    @BeforeEach
    void setUp() {
        requestId = new RequestId(1L);
        exchangeId = new ExchangeId("exchange-123");
    }

    @Test
    @DisplayName("RequestId로 ExchangeRequest를 조회한다")
    void loadExchangeRequestWithRequestId() {
        // given
        // 1. ExchangeRequest 조회
        when(loadExchangeRequestPort.loadByRequestId(requestId)).thenReturn(exchangeRequest);

        // when
        var result = exchangeRequestLoader.loadExchangeRequest(requestId);

        // then
        // 1. ExchangeRequest 조회 확인
        verify(loadExchangeRequestPort).loadByRequestId(requestId);

        // 2. ExchangeRequest 반환 확인
        assert result == exchangeRequest;
    }

    @Test
    @DisplayName("ExchangeId로 ExchangeRequest를 조회한다")
    void loadExchangeRequestWithExchangeId() {
        // given
        // 1. ExchangeRequest 조회
        when(loadExchangeRequestPort.loadByExchangeId(exchangeId)).thenReturn(exchangeRequest);

        // when
        var result = exchangeRequestLoader.loadExchangeRequest(exchangeId);

        // then
        // 1. ExchangeRequest 조회 확인
        verify(loadExchangeRequestPort).loadByExchangeId(exchangeId);

        // 2. ExchangeRequest 반환 확인
        assert result == exchangeRequest;
    }
} 