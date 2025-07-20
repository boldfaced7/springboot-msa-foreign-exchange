package com.boldfaced7.fxexchange.exchange.adapter.out.persistence.deposit;

import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

@ToString
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class JpaDeposit {
    @Id
    private String depositId;
    private Long requestId;
    private String exchangeId;
    private String userId;

    @Enumerated(EnumType.STRING)
    private Direction direction;

    private boolean success;
    private LocalDateTime depositedAt;


    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        JpaDeposit that = (JpaDeposit) object;
        return Objects.equals(getDepositId(), that.getDepositId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getDepositId());
    }
}
