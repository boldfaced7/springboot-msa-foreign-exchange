package com.boldfaced7.fxexchange.exchange.adapter.out.persistence.deposit;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class DepositJpa {
    @Id
    private String depositId;
    private Long requestId;
    private String exchangeId;
    private String userId;
    private Direction direction;
    private boolean success;
    private LocalDateTime depositedAt;
}
