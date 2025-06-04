package com.boldfaced7.fxexchange.exchange.application.service;

import com.boldfaced7.fxexchange.exchange.application.port.in.CompleteWithdrawalCancelCommand;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeEventPublisher.SimpleEventPublisher;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestUpdater;
import com.boldfaced7.fxexchange.exchange.application.service.util.ExchangeRequestUpdater.ParamRequestUpdater;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import com.boldfaced7.fxexchange.exchange.domain.vo.WithdrawalCancelId;
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
class CompleteWithdrawalCancelServiceTest {

    @InjectMocks
    private CompleteWithdrawalCancelService completeWithdrawalCancelService;

    @Mock
    private ExchangeEventPublisher exchangeEventPublisher;

    @Mock
    private ExchangeRequestUpdater exchangeRequestUpdater;

    @Mock
    private ExchangeRequest exchangeRequest;

    private ExchangeId exchangeId;
    private WithdrawalCancelId withdrawalCancelId;
    private CompleteWithdrawalCancelCommand command;

    @Captor
    private ArgumentCaptor<ParamRequestUpdater<WithdrawalCancelId>> addWithdrawalCancelIdCaptor;

    @Captor
    private ArgumentCaptor<ExchangeEventPublisher.SimpleEventPublisher> withdrawalCancelledCaptor;

    @BeforeEach
    void setUp() {
        exchangeId = new ExchangeId("exchange-id");
        withdrawalCancelId = new WithdrawalCancelId("withdrawal-cancel-id");
        command = new CompleteWithdrawalCancelCommand(
                exchangeId,
                withdrawalCancelId,
                Direction.BUY
        );
    }

    @Test
    @DisplayName("출금 취소 완료 요청 시, 전달하는 람다를 검증한다")
    void completeWithdrawalCancel_Success() {
        // given
        doReturn(exchangeRequest).when(exchangeRequestUpdater).update(
                eq(command.exchangeId()),
                any(ParamRequestUpdater.class),
                eq(command.withdrawalCancelId())
        );
        doNothing().when(exchangeEventPublisher).publishEvents(
                eq(command.exchangeId()),
                any(SimpleEventPublisher.class)
        );
        // when
        completeWithdrawalCancelService.completeWithdrawalCancel(command);

        // then
        verify(exchangeRequestUpdater).update(
                eq(command.exchangeId()),
                addWithdrawalCancelIdCaptor.capture(),
                eq(command.withdrawalCancelId())
        );
        verify(exchangeEventPublisher).publishEvents(
                eq(command.exchangeId()),
                withdrawalCancelledCaptor.capture()
        );

        // 람다식의 동작 검증
        addWithdrawalCancelIdCaptor.getValue().update(exchangeRequest, withdrawalCancelId);
        verify(exchangeRequest).addWithdrawalCancelId(withdrawalCancelId);

        // 이벤트 발행 람다 검증
        withdrawalCancelledCaptor.getValue().publish(exchangeRequest);
        verify(exchangeRequest).withdrawalCancelled();
    }

}