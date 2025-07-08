package com.boldfaced7.fxexchange.exchange.application.port.out.deposit;

import com.boldfaced7.fxexchange.exchange.domain.model.Deposit;

public interface SaveDepositPort {
    void saveDeposit(Deposit deposit);
}
