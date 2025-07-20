package com.boldfaced7.fxexchange.exchange.domain.vo.cancel;

public record WithdrawalCancelId(String value) {
    public WithdrawalCancelId {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("출금 취소 트랜잭션 ID는 비어 있을 수 없습니다.");
    }
}
