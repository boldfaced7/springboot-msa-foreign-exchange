package com.boldfaced7.fxexchange.exchange.adapter.out.persistence.cancel;

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
public class JpaWithdrawalCancel {
    @Id
    private String withdrawalCancelId;
    private Long requestId;
    private String exchangeId;
    private String userId;

    @Enumerated(EnumType.STRING)
    private Direction direction;

    private boolean success;
    private LocalDateTime cancelledAt;

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        JpaWithdrawalCancel that = (JpaWithdrawalCancel) object;
        return Objects.equals(getWithdrawalCancelId(), that.getWithdrawalCancelId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getWithdrawalCancelId());
    }
}
