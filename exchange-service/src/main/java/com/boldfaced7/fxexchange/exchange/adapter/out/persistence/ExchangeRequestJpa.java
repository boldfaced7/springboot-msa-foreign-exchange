package com.boldfaced7.fxexchange.exchange.adapter.out.persistence;

import com.boldfaced7.fxexchange.exchange.domain.enums.CurrencyCode;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRequestJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long exchangeRequestId;
    private String exchangeId;
    private String userId;

    private Direction direction;
    private CurrencyCode baseCurrency;
    private CurrencyCode quoteCurrency;

    private int baseAmount;
    private int quoteAmount;
    private double exchangeRate;

    private String withdrawId;
    private String depositId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;    
}
