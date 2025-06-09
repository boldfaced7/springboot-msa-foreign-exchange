package com.boldfaced7.fxexchange.exchange.application.service.util;

import com.boldfaced7.fxexchange.exchange.application.port.out.LoadExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.port.out.UpdateExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestUpdater.ParamRequestUpdater;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestUpdater.SimpleRequestUpdater;
import com.boldfaced7.fxexchange.exchange.application.service.util.impl.ExchangeRequestUpdaterImpl;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeRequestUpdaterImplTest {

    @InjectMocks
    private ExchangeRequestUpdaterImpl exchangeRequestUpdater;

    @Mock
    private LoadExchangeRequestPort loadExchangeRequestPort;

    @Mock
    private UpdateExchangeRequestPort updateExchangeRequestPort;

    @Mock
    private ExchangeRequest exchangeRequest;

    @Mock
    private SimpleRequestUpdater simpleRequestUpdater;

    @Mock
    private ParamRequestUpdater<String> paramRequestUpdater;

    private ExchangeId exchangeId;
    private RequestId requestId;
    private String param;

    @BeforeEach
    void setUp() {
        exchangeId = new ExchangeId("exchange-123");
        requestId = new RequestId(1L);
        param = "test-param";
    }

    @Test
    @DisplayName("ExchangeRequest로 직접 업데이트 시, 단순 업데이트가 수행된다")
    void update_WithExchangeRequest_SimpleUpdate() {
        // given
        doNothing().when(simpleRequestUpdater).update(exchangeRequest);
        when(updateExchangeRequestPort.update(exchangeRequest)).thenReturn(exchangeRequest);

        // when
        ExchangeRequest result = exchangeRequestUpdater.update(exchangeRequest, simpleRequestUpdater);

        // then
        verify(simpleRequestUpdater).update(exchangeRequest);
        verify(updateExchangeRequestPort).update(exchangeRequest);
        assertThat(result).isEqualTo(exchangeRequest);
    }

    @Test
    @DisplayName("ExchangeRequest로 직접 업데이트 시, 파라미터 업데이트가 수행된다")
    void update_WithExchangeRequest_ParamUpdate() {
        // given
        doNothing().when(paramRequestUpdater).update(exchangeRequest, param);
        when(updateExchangeRequestPort.update(exchangeRequest)).thenReturn(exchangeRequest);

        // when
        ExchangeRequest result = exchangeRequestUpdater.update(exchangeRequest, paramRequestUpdater, param);

        // then
        verify(paramRequestUpdater).update(exchangeRequest, param);
        verify(updateExchangeRequestPort).update(exchangeRequest);
        assertThat(result).isEqualTo(exchangeRequest);
    }

    @Test
    @DisplayName("RequestId로 업데이트 시, 단순 업데이트가 수행된다")
    void update_WithRequestId_SimpleUpdate() {
        // given
        when(loadExchangeRequestPort.loadByRequestId(requestId)).thenReturn(exchangeRequest);
        doNothing().when(simpleRequestUpdater).update(exchangeRequest);
        when(updateExchangeRequestPort.update(exchangeRequest)).thenReturn(exchangeRequest);

        // when
        ExchangeRequest result = exchangeRequestUpdater.update(requestId, simpleRequestUpdater);

        // then
        verify(loadExchangeRequestPort).loadByRequestId(requestId);
        verify(simpleRequestUpdater).update(exchangeRequest);
        verify(updateExchangeRequestPort).update(exchangeRequest);
        assertThat(result).isEqualTo(exchangeRequest);
    }

    @Test
    @DisplayName("RequestId로 업데이트 시, 파라미터 업데이트가 수행된다")
    void update_WithRequestId_ParamUpdate() {
        // given
        when(loadExchangeRequestPort.loadByRequestId(requestId)).thenReturn(exchangeRequest);
        doNothing().when(paramRequestUpdater).update(exchangeRequest, param);
        when(updateExchangeRequestPort.update(exchangeRequest)).thenReturn(exchangeRequest);

        // when
        ExchangeRequest result = exchangeRequestUpdater.update(requestId, paramRequestUpdater, param);

        // then
        verify(loadExchangeRequestPort).loadByRequestId(requestId);
        verify(paramRequestUpdater).update(exchangeRequest, param);
        verify(updateExchangeRequestPort).update(exchangeRequest);
        assertThat(result).isEqualTo(exchangeRequest);
    }

    @Test
    @DisplayName("ExchangeId로 업데이트 시, 단순 업데이트가 수행된다")
    void update_WithExchangeId_SimpleUpdate() {
        // given
        when(loadExchangeRequestPort.loadByExchangeId(exchangeId)).thenReturn(exchangeRequest);
        doNothing().when(simpleRequestUpdater).update(exchangeRequest);
        when(updateExchangeRequestPort.update(exchangeRequest)).thenReturn(exchangeRequest);

        // when
        ExchangeRequest result = exchangeRequestUpdater.update(exchangeId, simpleRequestUpdater);

        // then
        verify(loadExchangeRequestPort).loadByExchangeId(exchangeId);
        verify(simpleRequestUpdater).update(exchangeRequest);
        verify(updateExchangeRequestPort).update(exchangeRequest);
        assertThat(result).isEqualTo(exchangeRequest);
    }

    @Test
    @DisplayName("ExchangeId로 업데이트 시, 파라미터 업데이트가 수행된다")
    void update_WithExchangeId_ParamUpdate() {
        // given
        when(loadExchangeRequestPort.loadByExchangeId(exchangeId)).thenReturn(exchangeRequest);
        doNothing().when(paramRequestUpdater).update(exchangeRequest, param);
        when(updateExchangeRequestPort.update(exchangeRequest)).thenReturn(exchangeRequest);

        // when
        ExchangeRequest result = exchangeRequestUpdater.update(exchangeId, paramRequestUpdater, param);

        // then
        verify(loadExchangeRequestPort).loadByExchangeId(exchangeId);
        verify(paramRequestUpdater).update(exchangeRequest, param);
        verify(updateExchangeRequestPort).update(exchangeRequest);
        assertThat(result).isEqualTo(exchangeRequest);
    }
} 