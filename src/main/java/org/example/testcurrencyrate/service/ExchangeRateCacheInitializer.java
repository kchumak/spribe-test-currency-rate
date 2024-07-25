package org.example.testcurrencyrate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ExchangeRateCacheInitializer implements ApplicationRunner
{

    private final ExchangeRateCache exchangeRateCache;

    @Override
    public void run(ApplicationArguments args)
    {
        log.debug("Warming up exchange rate cache....");
        exchangeRateCache.refreshRates();
    }
}
