package org.example.testcurrencyrate.service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.example.testcurrencyrate.dao.entity.CurrencyEntity;
import org.example.testcurrencyrate.dao.entity.ExchangeRateEntity;
import org.example.testcurrencyrate.dao.repository.CurrencyRepository;
import org.example.testcurrencyrate.dao.repository.ExchangeRateRepository;
import org.example.testcurrencyrate.exception.ApplicationException;
import org.example.testcurrencyrate.external.client.ExchangeRateClient;
import org.example.testcurrencyrate.model.ExchangeRateDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

@Slf4j
@Component
public class InMemoryExchangeRateCache implements ExchangeRateCache
{
    private final CurrencyRepository currencyRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final ExchangeRateClient exchangeRateClient;
    private final TransactionTemplate transactionTemplate;

    private final Lock lock;
    private final Map<String, List<ExchangeRateDto>> cache;

    @Autowired
    public InMemoryExchangeRateCache(CurrencyRepository currencyRepository,
                                     ExchangeRateRepository exchangeRateRepository,
                                     ExchangeRateClient exchangeRateClient, TransactionTemplate transactionTemplate)
    {
        this.currencyRepository = currencyRepository;
        this.exchangeRateRepository = exchangeRateRepository;
        this.exchangeRateClient = exchangeRateClient;
        this.transactionTemplate = transactionTemplate;
        this.lock = new ReentrantLock();
        this.cache = new ConcurrentHashMap<>();
    }

    // for test purposes only
    InMemoryExchangeRateCache(CurrencyRepository currencyRepository, ExchangeRateRepository exchangeRateRepository,
                              ExchangeRateClient exchangeRateClient, TransactionTemplate transactionTemplate,
                              Lock lock, Map<String, List<ExchangeRateDto>> cache)
    {
        this.currencyRepository = currencyRepository;
        this.exchangeRateRepository = exchangeRateRepository;
        this.exchangeRateClient = exchangeRateClient;
        this.transactionTemplate = transactionTemplate;
        this.lock = lock;
        this.cache = cache;
    }

    @Override
    public Set<String> getCurrencies()
    {
        return cache.keySet();
    }

    @Override
    public void addCurrency(@NonNull String currency)
    {
        var newCurrency = currency.toUpperCase();
        if (cache.containsKey(newCurrency))
        {
            throw new ApplicationException("Currency already exists");
        }

        currencyRepository.save(new CurrencyEntity(newCurrency));
        cache.put(newCurrency, List.of()); // init cache with empty rates
        log.info("New currency added, rates appear soon: {}.", newCurrency);

        // Add currency is rare operation, and we keep rates in cache only for supported by app currencies.
        // So, when a new currency added, we need to refresh entire cache.
        // If necessary we can refresh rates in a new thread to do not block api call.
        refreshRates();
    }

    @Override
    public List<ExchangeRateDto> getRates(String currency)
    {
        var rateList = cache.get(currency);
        if (rateList == null)
        {
            throw new ApplicationException("Currency " + currency + " not supported");
        }
        return List.copyOf(rateList);
    }

    @Override
    public void refreshRates()
    {
        log.info("Refreshing exchange rates...");

        // only one refresh call can refresh all rates at the same time
        lock.lock();
        try
        {
            var availableCurrencies = getCurrencies();
            Consumer<String> runInTrx = (String baseCurrency) -> transactionTemplate.execute(txStatus ->
            {
                try
                {
                    refreshRatesForCurrency(baseCurrency, availableCurrencies);
                } catch (Exception e)
                {
                    log.error("Error during scheduled exchange rates update for currency {}", baseCurrency, e);
                }
                return null;
            });

            // Refresh every currency in a new transaction. If any fails it doesn't affect others.
            availableCurrencies.parallelStream().forEach(runInTrx);
        } finally
        {
            lock.unlock();
            log.info("Refreshing exchange rates finished");
        }
    }

    void refreshRatesForCurrency(@NonNull String baseCurrency, Set<String> targetCurrencies)
    {
        var now = Instant.now();
        log.debug("Refreshing rates for currency {}, timestamp {}", baseCurrency, now);

        // receive rates from 3rd party provider
        var ratesMap = obtainRatesForCurrency(baseCurrency);

        // filter out only rates for currencies supported by our application, others ignored
        var rates = ratesMap.entrySet().stream()
                .filter(entry -> targetCurrencies.contains(entry.getKey()))
                .map(entry -> ExchangeRateEntity.builder()
                        .baseCurrency(baseCurrency)
                        .targetCurrency(entry.getKey())
                        .rate(entry.getValue())
                        .timestamp(now)
                        .build())
                .toList();

        // persist to DB
        exchangeRateRepository.saveAll(rates);

        // update cache
        var newRates = rates.stream()
                .map(rate -> new ExchangeRateDto(rate.getTargetCurrency(), rate.getRate(), rate.getTimestamp()))
                .toList();
        cache.put(baseCurrency, newRates);

        log.debug("Rates for currency {} successfully saved", baseCurrency);
    }

    Map<String, BigDecimal> obtainRatesForCurrency(@NonNull String baseCurrency)
    {
        log.info("Obtaining exchange rate for currency {}...", baseCurrency);
        var response = exchangeRateClient.getExchangeRates(baseCurrency);
        if (response == null || !response.isSuccessful())
        {
            throw new ApplicationException("Could not fetch exchange rate for currency " + baseCurrency);
        }
        if (response.isNoRatesInResponse())
        {
            throw new ApplicationException("Response does not contain rates for currency " + baseCurrency);
        }

        log.debug("Received rates for currency {}: {}...", baseCurrency, response.getRates());
        return response.getRates();
    }
}
