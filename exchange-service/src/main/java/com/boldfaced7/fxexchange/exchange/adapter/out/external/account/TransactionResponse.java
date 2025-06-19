package com.boldfaced7.fxexchange.exchange.adapter.out.external.account;

public record TransactionResponse(
        boolean success,
        String status,
        String transactionId
) {
}
