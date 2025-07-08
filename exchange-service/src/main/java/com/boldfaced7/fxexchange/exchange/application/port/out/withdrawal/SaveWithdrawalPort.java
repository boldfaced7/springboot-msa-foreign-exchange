package com.boldfaced7.fxexchange.exchange.application.port.out.withdrawal;

import com.boldfaced7.fxexchange.exchange.domain.model.Withdrawal;

public interface SaveWithdrawalPort {
    void saveWithdrawal(Withdrawal withdrawal);
}
