package com.boldfaced7.fxexchange.exchange.application.config;

import com.boldfaced7.fxexchange.exchange.application.port.out.*;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@TestConfiguration
public class ApplicationTestConfig {
    
    private static <T> Map<Direction, T> toDirectionMap(T t) {
        return Arrays.stream(Direction.values()).collect(Collectors.toMap(
                Function.identity(),
                direction -> t
        ));
    }

    // LoadDepositResultPort
    @Bean
    public Map<Direction, LoadDepositResultPort> loadDepositResultPortMap(
            LoadDepositResultPort loadDepositResultPort
    ) {
        return toDirectionMap(loadDepositResultPort);
    }

    // LoadWithdrawalResultPort
    @Bean
    public Map<Direction, LoadWithdrawalResultPort> loadWithdrawalResultPortMap(
            LoadWithdrawalResultPort loadWithdrawalResultPort
    ) {
        return toDirectionMap(loadWithdrawalResultPort);
    }

    // RequestDepositPort
    @Bean
    public Map<Direction, RequestDepositPort> requestDepositPortMap(
            RequestDepositPort requestDepositPort
    ) {
        return toDirectionMap(requestDepositPort);
    }

    // RequestWithdrawalPort
    @Bean
    public Map<Direction, RequestWithdrawalPort> requestWithdrawalPortMap(
            RequestWithdrawalPort requestWithdrawalPort
    ) {
        return toDirectionMap(requestWithdrawalPort);
    }

    // SendDepositCheckRequestPort
    @Bean
    public Map<Direction, SendDepositCheckRequestPort> sendDepositCheckRequestPortMap(
            SendDepositCheckRequestPort sendDepositCheckRequestPort
    ) {
        return toDirectionMap(sendDepositCheckRequestPort);
    }

    // SendWithdrawalCheckRequestPort
    @Bean
    public Map<Direction, SendWithdrawalCheckRequestPort> sendWithdrawalCheckRequestPortMap(
            SendWithdrawalCheckRequestPort sendWithdrawalCheckRequestPort
    ) {
        return toDirectionMap(sendWithdrawalCheckRequestPort);
    }

    // UndoWithdrawalPort
    @Bean
    public Map<Direction, CancelWithdrawalPort> undoWithdrawalPortMap(
            CancelWithdrawalPort cancelWithdrawalPort
    ) {
        return toDirectionMap(cancelWithdrawalPort);
    }
}
