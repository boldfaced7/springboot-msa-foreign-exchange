package com.boldfaced7.fxexchange.exchange.adapter.out.persistence.withdrawal;

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
public class JpaWithdrawal {
    @Id
    private String withdrawalId;
    private Long requestId;
    private String exchangeId;
    private String userId;

    @Enumerated(EnumType.STRING)
    private Direction direction;

    private boolean success;
    private LocalDateTime withdrawnAt;

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        JpaWithdrawal that = (JpaWithdrawal) object;
        return Objects.equals(getWithdrawalId(), that.getWithdrawalId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getWithdrawalId());
    }
}
