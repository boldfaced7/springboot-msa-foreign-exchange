package com.boldfaced7.fxexchange.exchange.application.service;

import com.boldfaced7.fxexchange.exchange.application.port.in.CheckDepositWithDelayCommand;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher.ParamEventPublisher;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class CheckDepositWithDelayServiceTest {

    @InjectMocks
    private CheckDepositWithDelayService checkDepositWithDelayService;

    @Mock
    private ExchangeEventPublisher exchangeEventPublisher;

    @Mock
    private ExchangeRequest exchangeRequest;

    private ExchangeId exchangeId;
    private Count count;
    private CheckDepositWithDelayCommand command;

    @Captor
    private ArgumentCaptor<ExchangeEventPublisher.ParamEventPublisher<Count>> depositResultUnknownCaptor;

    @BeforeEach
    void setUp() {
        exchangeId = new ExchangeId("exchangeId");
        count = new Count(1);
        command = new CheckDepositWithDelayCommand(exchangeId, count, Direction.BUY);
    }

    @Test
    @DisplayName("입금 확인 요청 시, 전달되는 람다를 검증한다")
    void checkDepositWithDelay_Success() {
        // given
        doNothing().when(exchangeEventPublisher).publishEvents(
                eq(exchangeId),
                any(ParamEventPublisher.class),
                eq(count)
        );
        // when
        checkDepositWithDelayService.checkDepositWithDelay(command);

        // then
        verify(exchangeEventPublisher).publishEvents(
                eq(exchangeId),
                depositResultUnknownCaptor.capture(),
                eq(count)
        );
        // 람다식의 동작 검증
        // 입금 결과 알 수 없음 람다 검증
        depositResultUnknownCaptor.getValue().publish(exchangeRequest, count);
        verify(exchangeRequest).depositResultUnknown(count);
    }
}