package com.boldfaced7.fxexchange.exchange.application.service.util;

import com.boldfaced7.fxexchange.exchange.application.port.out.PublishExchangeEventPort;
import com.boldfaced7.fxexchange.exchange.application.service.util.impl.ExchangeEventPublisherImpl;
import com.boldfaced7.fxexchange.exchange.domain.event.DomainEvent;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeEventPublisherImplTest {

    @InjectMocks
    private ExchangeEventPublisherImpl exchangeEventPublisher;

    @Mock
    private ExchangeRequestLoader exchangeRequestLoader;

    @Mock
    private PublishExchangeEventPort publishExchangeEventPort;

    @Mock
    private ExchangeRequest exchangeRequest;

    private RequestId requestId;
    private ExchangeId exchangeId;
    private List<DomainEvent> events;

    @BeforeEach
    void setUp() {
        requestId = new RequestId(1L);
        exchangeId = new ExchangeId("exchange-123");
        events = new ArrayList<>();
        when(exchangeRequest.pullEvents()).thenReturn(events);
    }

    @Test
    @DisplayName("ExchangeRequest로 단순 이벤트를 생성하고 발행한다")
    void publishEventsWithExchangeRequest() {
        // given
        doNothing().when(exchangeRequest).exchangeCurrencySucceeded();
        doNothing().when(publishExchangeEventPort).publish(events);

        // when
        exchangeEventPublisher.publishEvents(exchangeRequest, ExchangeRequest::exchangeCurrencySucceeded);

        // then
        verify(exchangeRequest).exchangeCurrencySucceeded();
        verify(publishExchangeEventPort).publish(events);
    }

    @Test
    @DisplayName("RequestId로 단순 이벤트를 생성하고 발행한다")
    void publishEventsWithRequestId() {
        // given
        when(exchangeRequestLoader.loadExchangeRequest(requestId)).thenReturn(exchangeRequest);
        doNothing().when(exchangeRequest).exchangeCurrencySucceeded();
        doNothing().when(publishExchangeEventPort).publish(events);

        // when
        exchangeEventPublisher.publishEvents(requestId, ExchangeRequest::exchangeCurrencySucceeded);

        // then
        verify(exchangeRequestLoader).loadExchangeRequest(requestId);
        verify(exchangeRequest).exchangeCurrencySucceeded();
        verify(publishExchangeEventPort).publish(events);
    }

    @Test
    @DisplayName("ExchangeId로 단순 이벤트를 생성하고 발행한다")
    void publishEventsWithExchangeId() {
        // given
        when(exchangeRequestLoader.loadExchangeRequest(exchangeId)).thenReturn(exchangeRequest);
        doNothing().when(exchangeRequest).exchangeCurrencySucceeded();
        doNothing().when(publishExchangeEventPort).publish(events);

        // when
        exchangeEventPublisher.publishEvents(exchangeId, ExchangeRequest::exchangeCurrencySucceeded);

        // then
        verify(exchangeRequestLoader).loadExchangeRequest(exchangeId);
        verify(exchangeRequest).exchangeCurrencySucceeded();
        verify(publishExchangeEventPort).publish(events);
    }

    @Test
    @DisplayName("ExchangeRequest로 파라미터가 있는 이벤트를 생성하고 발행한다")
    void publishEventsWithExchangeRequestAndParam() {
        // given
        Count count = Count.zero();
        doNothing().when(exchangeRequest).delayingWithdrawalCheckRequired(count);
        doNothing().when(publishExchangeEventPort).publish(events);

        // when
        exchangeEventPublisher.publishEvents(exchangeRequest, ExchangeRequest::delayingWithdrawalCheckRequired, count);

        // then
        verify(exchangeRequest).delayingWithdrawalCheckRequired(count);
        verify(publishExchangeEventPort).publish(events);
    }

    @Test
    @DisplayName("RequestId로 파라미터가 있는 이벤트를 생성하고 발행한다")
    void publishEventsWithRequestIdAndParam() {
        // given
        Count count = Count.zero();
        when(exchangeRequestLoader.loadExchangeRequest(requestId)).thenReturn(exchangeRequest);
        doNothing().when(exchangeRequest).delayingWithdrawalCheckRequired(count);
        doNothing().when(publishExchangeEventPort).publish(events);

        // when
        exchangeEventPublisher.publishEvents(requestId, ExchangeRequest::delayingWithdrawalCheckRequired, count);

        // then
        verify(exchangeRequestLoader).loadExchangeRequest(requestId);
        verify(exchangeRequest).delayingWithdrawalCheckRequired(count);
        verify(publishExchangeEventPort).publish(events);
    }

    @Test
    @DisplayName("ExchangeId로 파라미터가 있는 이벤트를 생성하고 발행한다")
    void publishEventsWithExchangeIdAndParam() {
        // given
        Count count = Count.zero();
        when(exchangeRequestLoader.loadExchangeRequest(exchangeId)).thenReturn(exchangeRequest);
        doNothing().when(exchangeRequest).delayingWithdrawalCheckRequired(count);
        doNothing().when(publishExchangeEventPort).publish(events);

        // when
        exchangeEventPublisher.publishEvents(exchangeId, ExchangeRequest::delayingWithdrawalCheckRequired, count);

        // then
        verify(exchangeRequestLoader).loadExchangeRequest(exchangeId);
        verify(exchangeRequest).delayingWithdrawalCheckRequired(count);
        verify(publishExchangeEventPort).publish(events);
    }

    @Test
    @DisplayName("ExchangeRequest의 이벤트를 발행한다")
    void publishEvents() {
        // given
        doNothing().when(publishExchangeEventPort).publish(events);

        // when
        exchangeEventPublisher.publishEvents(exchangeRequest);

        // then
        verify(exchangeRequest).pullEvents();
        verify(publishExchangeEventPort).publish(events);
    }
} 