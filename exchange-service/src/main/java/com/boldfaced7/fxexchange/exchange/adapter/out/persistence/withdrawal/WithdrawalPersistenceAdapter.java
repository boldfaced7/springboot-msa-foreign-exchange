package com.boldfaced7.fxexchange.exchange.adapter.out.persistence.withdrawal;

import com.boldfaced7.fxexchange.exchange.application.port.out.withdrawal.SaveWithdrawalPort;
import com.boldfaced7.fxexchange.exchange.domain.model.Withdrawal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WithdrawalPersistenceAdapter implements SaveWithdrawalPort {
    private final JpaWithdrawalRepository jpaWithdrawalRepository;

    @Override
    public void saveWithdrawal(Withdrawal withdrawal) {
        jpaWithdrawalRepository.save(WithdrawalMapper.toJpa(withdrawal));
    }
    
}
