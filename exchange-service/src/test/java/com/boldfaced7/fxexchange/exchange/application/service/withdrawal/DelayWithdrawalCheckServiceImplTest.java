package com.boldfaced7.fxexchange.exchange.application.service.withdrawal;

import com.boldfaced7.fxexchange.exchange.application.port.out.SendWithdrawalCheckRequestPort;
import com.boldfaced7.fxexchange.exchange.application.service.withdrawal.impl.DelayWithdrawalCheckServiceImpl;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.ExchangeId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DelayWithdrawalCheckServiceImplTest {

    @InjectMocks
    private DelayWithdrawalCheckServiceImpl delayWithdrawalCheckService;

    @Mock
    private SendWithdrawalCheckRequestPort sendWithdrawalCheckPort;

    private ExchangeId exchangeId;

    @BeforeEach
    void setUp() {
        exchangeId = new ExchangeId("exchangeId");
    }

    @Test
    @DisplayName("지연 시간이 재시도 횟수에 비례하여 증가해야 한다")
    void delayWithdrawalCheck() {
        // given
        Count count = new Count(2);
        Duration expectedDelay = Duration.ofSeconds(30).multipliedBy(count.value() + 1);

        // when
        delayWithdrawalCheckService.delayWithdrawalCheck(exchangeId, count, Direction.BUY);

        // then
        verify(sendWithdrawalCheckPort).sendWithdrawalCheckRequest(
                eq(exchangeId),
                argThat(duration -> duration.equals(expectedDelay)),
                eq(count),
                eq(Direction.BUY)
        );
    }

    @Test
    @DisplayName("재시도 횟수가 0이면 기본 지연 시간이 적용되어야 한다")
    void delayWithdrawalCheckZeroCount() {
        // given
        Count count = Count.zero();
        Duration expectedDelay = Duration.ofSeconds(30);

        // when
        delayWithdrawalCheckService.delayWithdrawalCheck(exchangeId, count, Direction.BUY);

        // then
        verify(sendWithdrawalCheckPort).sendWithdrawalCheckRequest(
                eq(exchangeId),
                argThat(duration -> duration.equals(expectedDelay)),
                eq(count),
                eq(Direction.BUY)
        );
    }
} 