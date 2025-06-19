package com.boldfaced7.fxexchange.exchange.adapter.out;

import com.boldfaced7.fxexchange.exchange.adapter.out.external.account.FxAccountHttpClient;
import com.boldfaced7.fxexchange.exchange.adapter.out.external.account.KrwAccountHttpClient;
import com.boldfaced7.fxexchange.exchange.adapter.out.external.account.LoadTransactionClient;
import com.boldfaced7.fxexchange.exchange.adapter.out.external.account.RequestTransactionClient;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class AccountTransactionClientMapper {

    private final Map<Direction, LoadTransactionClient> loadDepositClients = new EnumMap<>(Direction.class);
    private final Map<Direction, LoadTransactionClient> loadWithdrawalClients = new EnumMap<>(Direction.class);
    private final Map<Direction, RequestTransactionClient> requestDepositClients = new  EnumMap<>(Direction.class);
    private final Map<Direction, RequestTransactionClient> requestWithdrawalClients = new  EnumMap<>(Direction.class);


    public AccountTransactionClientMapper(
            FxAccountHttpClient fxAccountHttpClient,
            KrwAccountHttpClient krwAccountHttpClient
    ) {
        loadDepositClients.put(Direction.BUY, fxAccountHttpClient);
        loadDepositClients.put(Direction.SELL, krwAccountHttpClient);

        requestDepositClients.put(Direction.BUY, fxAccountHttpClient);
        requestDepositClients.put(Direction.SELL, krwAccountHttpClient);


        loadWithdrawalClients.put(Direction.BUY, krwAccountHttpClient);
        loadWithdrawalClients.put(Direction.SELL, fxAccountHttpClient);

        requestWithdrawalClients.put(Direction.BUY, krwAccountHttpClient);
        requestWithdrawalClients.put(Direction.SELL, fxAccountHttpClient);
    }

    public LoadTransactionClient getWithdrawalResultClient(Direction direction) {
        return loadWithdrawalClients.get(direction);
    }

    public LoadTransactionClient getDepositResultClient(Direction direction) {
        return loadDepositClients.get(direction);
    }

    public RequestTransactionClient getWithdrawalClient(Direction direction) {
        return requestWithdrawalClients.get(direction);
    }

    public RequestTransactionClient getDepositClient(Direction direction) {
        return requestDepositClients.get(direction);
    }

}
