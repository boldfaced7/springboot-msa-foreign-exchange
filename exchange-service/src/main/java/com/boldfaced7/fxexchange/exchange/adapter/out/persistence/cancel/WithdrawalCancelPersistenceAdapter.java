package com.boldfaced7.fxexchange.exchange.adapter.out.persistence.cancel;

import com.boldfaced7.fxexchange.exchange.application.port.out.cancel.SaveWithdrawalCancelPort;
import com.boldfaced7.fxexchange.exchange.domain.model.WithdrawalCancel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WithdrawalCancelPersistenceAdapter implements SaveWithdrawalCancelPort {
    private final JpaWithdrawalCancelRepository jpaWithdrawalCancelRepository;

    @Override
    public void saveWithdrawalCancel(WithdrawalCancel withdrawalCancel) {
        jpaWithdrawalCancelRepository.save(WithdrawalCancelMapper.toJpa(withdrawalCancel));
    }
    
}
