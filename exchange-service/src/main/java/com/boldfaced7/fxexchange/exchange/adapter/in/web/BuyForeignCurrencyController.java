package com.boldfaced7.fxexchange.exchange.adapter.in.web;

import com.boldfaced7.fxexchange.common.WebAdapter;
import com.boldfaced7.fxexchange.exchange.application.port.in.BuyForeignCurrencyCommand;
import com.boldfaced7.fxexchange.exchange.application.port.in.BuyForeignCurrencyUseCase;
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
public class BuyForeignCurrencyController {

    private final BuyForeignCurrencyUseCase buyForeignCurrencyUseCase;

    @PostMapping("/buy")
    public ResponseEntity<Response> buyForeignCurrency(
        @RequestBody Request request
    ) {
        var command = toCommand(request);
        var requested = buyForeignCurrencyUseCase.buy(command);
        var response = toResponse(requested);

        return ResponseEntity.ok(response);
    }

    private BuyForeignCurrencyCommand toCommand(Request request) {
        return new BuyForeignCurrencyCommand(
            new UserId(request.userId()),
            new QuoteCurrency(request.quoteCurrency()),
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
        CurrencyCode quoteCurrency,
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
