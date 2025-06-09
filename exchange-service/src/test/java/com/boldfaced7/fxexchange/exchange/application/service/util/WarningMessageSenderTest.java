package com.boldfaced7.fxexchange.exchange.application.service.util;

import com.boldfaced7.fxexchange.exchange.application.port.out.SendWarningMessagePort;
import com.boldfaced7.fxexchange.exchange.application.service.util.impl.WarningMessageSenderImpl;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WarningMessageSenderTest {

    @InjectMocks
    private WarningMessageSenderImpl warningMessageSender;

    @Mock
    private SendWarningMessagePort sendWarningMessagePort;

    private RequestId requestId;
    private ExchangeId exchangeId;

    @BeforeEach
    void setUp() {
        requestId = new RequestId(1L);
        exchangeId = new ExchangeId("exchange-123");
    }

    @Test
    @DisplayName("경고 메시지를 전송한다")
    void sendWarningMessage() {
        // given
        // 1. 경고 메시지 전송
        doNothing().when(sendWarningMessagePort).sendWarningMessage(requestId, exchangeId);

        // when
        warningMessageSender.sendWarningMessage(requestId, exchangeId);

        // then
        // 1. 경고 메시지 전송 확인
        verify(sendWarningMessagePort).sendWarningMessage(requestId, exchangeId);
    }
} 