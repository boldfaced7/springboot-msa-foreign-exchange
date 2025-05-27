package com.boldfaced7.fxexchange.exchange.application.service.buy.withdrawal;

import com.boldfaced7.fxexchange.exchange.application.port.out.buy.SendKrwWithdrawalCheckRequestPort;
import com.boldfaced7.fxexchange.exchange.domain.vo.Count;
import com.boldfaced7.fxexchange.exchange.domain.vo.RequestId;
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
class DelayKrwWithdrawalCheckServiceImplTest {

    @InjectMocks
    private DelayKrwWithdrawalCheckServiceImpl delayKrwWithdrawalCheckService;

    @Mock
    private SendKrwWithdrawalCheckRequestPort sendKrwWithdrawalCheckRequestPort;

    private RequestId requestId;

    @BeforeEach
    void setUp() {
        requestId = new RequestId(1L);
    }

    @Test
    @DisplayName("지연 시간이 재시도 횟수에 비례하여 증가해야 한다")
    void delayKrwWithdrawalCheck() {
        // given
        Count count = new Count(2);
        Duration expectedDelay = Duration.ofSeconds(30).multipliedBy(count.value() + 1);

        // when
        delayKrwWithdrawalCheckService.delayKrwWithdrawalCheck(requestId, count);

        // then
        verify(sendKrwWithdrawalCheckRequestPort).sendKrwWithdrawalCheckRequest(
            eq(requestId), 
            argThat(duration -> duration.equals(expectedDelay))
        );
    }

    @Test
    @DisplayName("재시도 횟수가 0이면 기본 지연 시간이 적용되어야 한다")
    void delayKrwWithdrawalCheckZeroCount() {
        // given
        Count count = Count.zero();
        Duration expectedDelay = Duration.ofSeconds(30);

        // when
        delayKrwWithdrawalCheckService.delayKrwWithdrawalCheck(requestId, count);

        // then
        verify(sendKrwWithdrawalCheckRequestPort).sendKrwWithdrawalCheckRequest(
            eq(requestId), 
            argThat(duration -> duration.equals(expectedDelay))
        );
    }
} 