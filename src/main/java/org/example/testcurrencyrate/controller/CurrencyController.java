package org.example.testcurrencyrate.controller;

import lombok.RequiredArgsConstructor;
import org.example.testcurrencyrate.model.CurrencyDto;
import org.example.testcurrencyrate.service.ExchangeRateCache;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/public/v1/currencies")
@RequiredArgsConstructor
public class CurrencyController
{

    private final ExchangeRateCache exchangeRateCache;

    @GetMapping
    public Set<String> getAllCurrencies()
    {
        // if necessary we can add pagination later
        return exchangeRateCache.getCurrencies();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addCurrency(@RequestBody CurrencyDto currencyDto)
    {
        exchangeRateCache.addCurrency(currencyDto.currencyCode());
    }
}
