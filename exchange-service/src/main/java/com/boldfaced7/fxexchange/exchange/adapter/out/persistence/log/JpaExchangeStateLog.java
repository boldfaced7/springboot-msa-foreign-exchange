package com.boldfaced7.fxexchange.exchange.adapter.out.persistence.log;


import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.enums.ExchangeState;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

@ToString
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class JpaExchangeStateLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;
    private Long requestId;

    @Enumerated(EnumType.STRING)
    private Direction direction;

    @Enumerated(EnumType.STRING)
    private ExchangeState state;

    private LocalDateTime raisedAt;

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        JpaExchangeStateLog that = (JpaExchangeStateLog) object;
        return Objects.equals(getLogId(), that.getLogId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getLogId());
    }
}
