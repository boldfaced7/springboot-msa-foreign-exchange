package com.boldfaced7.fxexchange.exchange.domain.vo;

import lombok.Getter;

public record WithdrawalResult(
        @Getter boolean success,
        AccountCommandStatus status,
        WithdrawalId withdrawalId
) {}
