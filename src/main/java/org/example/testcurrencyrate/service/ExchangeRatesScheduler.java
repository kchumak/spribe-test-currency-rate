package org.example.testcurrencyrate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@RequiredArgsConstructor
public class ExchangeRatesScheduler
{
    private final ExchangeRateCache exchangeRateCache;

    @Scheduled(cron = "${application.fetch.rates.cron}")
    public void getRates()
    {
        log.info("Scheduled refresh exchange rates...");
        exchangeRateCache.refreshRates();
    }
}
