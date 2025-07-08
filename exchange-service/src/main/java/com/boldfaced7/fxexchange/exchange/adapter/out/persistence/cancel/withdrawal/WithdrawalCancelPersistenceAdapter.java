package com.boldfaced7.fxexchange.exchange.adapter.out.persistence.cancel.withdrawal;

import com.boldfaced7.fxexchange.exchange.application.port.out.cancel.SaveWithdrawalCancelPort;
import com.boldfaced7.fxexchange.exchange.domain.model.WithdrawalCancel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WithdrawalCancelPersistenceAdapter implements SaveWithdrawalCancelPort {
    private final WithdrawalCancelJpaRepository withdrawalCancelJpaRepository;

    @Override
    public void saveWithdrawalCancel(WithdrawalCancel withdrawalCancel) {
        withdrawalCancelJpaRepository.save(WithdrawalCancelMapper.toJpa(withdrawalCancel));
    }
    
}
