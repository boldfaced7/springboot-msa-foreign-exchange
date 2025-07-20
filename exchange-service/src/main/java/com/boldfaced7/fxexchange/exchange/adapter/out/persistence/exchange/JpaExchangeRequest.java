package com.boldfaced7.fxexchange.exchange.adapter.out.persistence.exchange;

import com.boldfaced7.fxexchange.exchange.domain.enums.CurrencyCode;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;


@ToString
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_exchange_id", columnNames = {"exchange_id"})
})
public class JpaExchangeRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;
    private String exchangeId;
    private String userId;

    @Enumerated(EnumType.STRING)
    private Direction direction;

    @Enumerated(EnumType.STRING)
    private CurrencyCode baseCurrency;
    @Enumerated(EnumType.STRING)
    private CurrencyCode quoteCurrency;

    private int baseAmount;
    private int quoteAmount;
    private double exchangeRate;

    private boolean exchanged;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        JpaExchangeRequest that = (JpaExchangeRequest) object;
        return Objects.equals(getRequestId(), that.getRequestId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getRequestId());
    }
}
