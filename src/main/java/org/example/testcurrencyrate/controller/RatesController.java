package org.example.testcurrencyrate.controller;

import lombok.RequiredArgsConstructor;
import org.example.testcurrencyrate.model.ExchangeRateDto;
import org.example.testcurrencyrate.service.ExchangeRateCache;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/v1/currency-rates")
@RequiredArgsConstructor
public class RatesController
{

    private final ExchangeRateCache exchangeRateCache;

    @GetMapping("/{baseCurrency}")
    public List<ExchangeRateDto> getCurrencyRates(@PathVariable String baseCurrency)
    {
        // if necessary we can add pagination later
        return exchangeRateCache.getRates(baseCurrency);
    }
}
