package com.boldfaced7.fxexchange.exchange.application.config;

import com.boldfaced7.fxexchange.exchange.application.port.out.*;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Profile("!test")
@Configuration
public class ApplicationConfig {

    // LoadDepositResultPort
    @Bean
    public Map<Direction, LoadDepositResultPort> loadDepositResultPortMap(
            List<LoadDepositResultPort> rawList
    ) {
        return rawList.stream().collect(Collectors.toMap(
                LoadDepositResultPort::direction,
                Function.identity()
        ));
    }

    // LoadWithdrawalResultPort
    @Bean
    public Map<Direction, LoadWithdrawalResultPort> loadWithdrawalResultPortMap(
            List<LoadWithdrawalResultPort> rawList
    ) {
        return rawList.stream().collect(Collectors.toMap(
                LoadWithdrawalResultPort::direction,
                Function.identity()
        ));
    }

    // RequestDepositPort
    @Bean
    public Map<Direction, RequestDepositPort> requestDepositPortMap(
            List<RequestDepositPort> rawList
    ) {
        return rawList.stream().collect(Collectors.toMap(
                RequestDepositPort::direction,
                Function.identity()
        ));
    }

    // RequestWithdrawalPort
    @Bean
    public Map<Direction, RequestWithdrawalPort> requestWithdrawalPortMap(
            List<RequestWithdrawalPort> rawList
    ) {
        return rawList.stream().collect(Collectors.toMap(
                RequestWithdrawalPort::direction,
                Function.identity()
        ));
    }

    // SendDepositCheckRequestPort
    @Bean
    public Map<Direction, SendDepositCheckRequestPort> sendDepositCheckRequestPortMap(
            List<SendDepositCheckRequestPort> rawList
    ) {
        return rawList.stream().collect(Collectors.toMap(
                SendDepositCheckRequestPort::direction,
                Function.identity()
        ));
    }

    // SendWithdrawalCheckRequestPort
    @Bean
    public Map<Direction, SendWithdrawalCheckRequestPort> sendWithdrawalCheckRequestPortMap(
            List<SendWithdrawalCheckRequestPort> rawList
    ) {
        return rawList.stream().collect(Collectors.toMap(
                SendWithdrawalCheckRequestPort::direction,
                Function.identity()
        ));
    }

    // UndoWithdrawalPort
    @Bean
    public Map<Direction, UndoWithdrawalPort> undoWithdrawalPortMap(
            List<UndoWithdrawalPort> rawList
    ) {
        return rawList.stream().collect(Collectors.toMap(
                UndoWithdrawalPort::direction,
                Function.identity()
        ));
    }
}
