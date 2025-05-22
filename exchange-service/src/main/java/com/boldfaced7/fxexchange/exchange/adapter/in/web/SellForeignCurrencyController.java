package com.boldfaced7.fxexchange.exchange.adapter.in.web;

import com.boldfaced7.fxexchange.common.WebAdapter;
import com.boldfaced7.fxexchange.exchange.application.port.in.SellForeignCurrencyCommand;
import com.boldfaced7.fxexchange.exchange.application.port.in.SellForeignCurrencyUseCase;
import com.boldfaced7.fxexchange.exchange.domain.enums.CurrencyCode;
import com.boldfaced7.fxexchange.exchange.domain.model.ExchangeRequest;
import com.boldfaced7.fxexchange.exchange.domain.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@WebAdapter
@RequestMapping("/exchange")
@RequiredArgsConstructor
public class SellForeignCurrencyController {

    private final SellForeignCurrencyUseCase sellForeignCurrencyUseCase;

    @PostMapping("/sell")
    public ResponseEntity<Response> sellForeignCurrency(
        @RequestBody Request request
    ) {
        var command = toCommand(request);
        var requested = sellForeignCurrencyUseCase.sell(command);
        var response = toResponse(requested);

        return ResponseEntity.ok(response);
    }

    private SellForeignCurrencyCommand toCommand(Request request) {
        return new SellForeignCurrencyCommand(
            new UserId(request.userId()),
            new BaseCurrency(request.baseCurrency()),
            new BaseAmount(request.baseAmount()),
            new QuoteAmount(request.quoteAmount()),
            new ExchangeRate(request.exchangeRate())
        );
    }

    private Response toResponse(ExchangeRequest exchangeRequest) {
        return new Response(
            exchangeRequest.getExchangeId().value(),
            exchangeRequest.getWithdrawId().value(),
            exchangeRequest.getDepositId().value()
        );
    }

    public record Request(
        String userId,
        CurrencyCode baseCurrency,
        int baseAmount,
        int quoteAmount,
        double exchangeRate
    ) {}

    public record Response(
        String exchangeId,
        String withdrawId,
        String depositId
    ) {}
}