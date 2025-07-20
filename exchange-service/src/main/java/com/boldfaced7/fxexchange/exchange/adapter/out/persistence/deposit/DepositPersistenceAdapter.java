package com.boldfaced7.fxexchange.exchange.adapter.out.persistence.deposit;

import com.boldfaced7.fxexchange.exchange.application.port.out.deposit.SaveDepositPort;
import com.boldfaced7.fxexchange.exchange.domain.model.Deposit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DepositPersistenceAdapter implements SaveDepositPort {
    private final JpaDepositRepository jpaDepositRepository;

    @Override
    public void saveDeposit(Deposit deposit) {
        jpaDepositRepository.save(DepositMapper.toJpa(deposit));
    }
    
}
