package com.boldfaced7.fxexchange.exchange.adapter.in.web;

import com.boldfaced7.fxexchange.common.WebAdapter;
import com.boldfaced7.fxexchange.exchange.application.port.in.ExchangeCurrencyCommand;
import com.boldfaced7.fxexchange.exchange.application.port.in.ExchangeCurrencyUseCase;
import com.boldfaced7.fxexchange.exchange.domain.enums.CurrencyCode;
import com.boldfaced7.fxexchange.exchange.domain.enums.Direction;
import com.boldfaced7.fxexchange.exchange.domain.vo.exchange.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@WebAdapter
@RequestMapping("/exchange")
@RequiredArgsConstructor
public class SellForeignCurrencyController {

    private final ExchangeCurrencyUseCase exchangeCurrencyUseCase;

    @PostMapping("/sell")
    public ResponseEntity<Response> sellForeignCurrency(
            @RequestBody Request request
    ) {
        var command = toCommand(request);
        var requested = exchangeCurrencyUseCase.exchangeCurrency(command);
        var response = toResponse(requested);

        return ResponseEntity.ok(response);
    }

    private ExchangeCurrencyCommand toCommand(Request request) {
        return new ExchangeCurrencyCommand(
                new ExchangeId(request.exchangeId()),
                new UserId(request.userId()),
                new BaseCurrency(request.baseCurrency()),
                new BaseAmount(request.baseAmount()),
                new QuoteAmount(request.quoteAmount()),
                Direction.SELL,
                new ExchangeRate(request.exchangeRate())
        );
    }

    private Response toResponse(ExchangeDetail exchangeDetail) {
        return new Response(
                exchangeDetail.exchangeRequest().getExchangeId().value(),
                exchangeDetail.withdrawal().getWithdrawalId().value(),
                exchangeDetail.deposit().getDepositId().value()
        );
    }

    public record Request(
            String exchangeId,
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
