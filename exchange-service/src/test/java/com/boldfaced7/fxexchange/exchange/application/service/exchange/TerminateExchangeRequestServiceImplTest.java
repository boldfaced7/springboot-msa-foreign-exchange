package com.boldfaced7.fxexchange.exchange.application.service.exchange;

import com.boldfaced7.fxexchange.exchange.application.port.out.UpdateExchangeRequestPort;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestLoader;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TerminateExchangeRequestServiceImplTest {

    @Mock
    private UpdateExchangeRequestPort updateExchangeRequestPort;

    @Mock
    private ExchangeRequestLoader exchangeRequestLoader;

    @Mock
    private ExchangeRequest exchangeRequest;

    @InjectMocks
    private TerminateExchangeRequestServiceImpl terminateExchangeRequestService;

    private RequestId requestId;

    @BeforeEach
    void setUp() {
        requestId = new RequestId(1L);
    }

    @Test
    @DisplayName("환전 요청이 정상적으로 종료되어야 한다")
    void terminateExchangeRequest_Success() {
        // given
        when(exchangeRequestLoader.loadExchangeRequest(requestId)).thenReturn(exchangeRequest);
        doNothing().when(exchangeRequest).terminate();
        when(updateExchangeRequestPort.update(exchangeRequest)).thenReturn(exchangeRequest);

        // when
        terminateExchangeRequestService.terminateExchangeRequest(requestId);

        // then
        verify(exchangeRequestLoader).loadExchangeRequest(requestId);
        verify(exchangeRequest).terminate();
        verify(updateExchangeRequestPort).update(exchangeRequest);
    }
} 