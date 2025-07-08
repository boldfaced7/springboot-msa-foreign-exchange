package com.boldfaced7.fxexchange.exchange.application.port.out.cancel;

import com.boldfaced7.fxexchange.exchange.domain.model.WithdrawalCancel;

public interface SaveWithdrawalCancelPort {
    void saveWithdrawalCancel(WithdrawalCancel withdrawalCancel);
}
