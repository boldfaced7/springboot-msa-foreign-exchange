package com.boldfaced7.fxexchange.exchange.domain.vo;

import lombok.Getter;

public record DepositResult(
        @Getter boolean success,
        AccountCommandStatus status,
        DepositId depositId
) {}
