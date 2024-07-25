package org.example.testcurrencyrate.service;

import lombok.NonNull;
import org.example.testcurrencyrate.model.ExchangeRateDto;

import java.util.List;
import java.util.Set;

/**
 * For multiple nodes any shared in memory solution can be used, f.e. Redis.
 * For test task purposes used java.util.Map.
 */
public interface ExchangeRateCache
{
    Set<String> getCurrencies();

    void addCurrency(@NonNull String currency);

    List<ExchangeRateDto> getRates(String currency);

    void refreshRates();
}
