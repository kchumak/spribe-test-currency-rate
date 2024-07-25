package org.example.testcurrencyrate.external.client;

import org.example.testcurrencyrate.external.dto.ExchangeRateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "exchangeRateClient", url = "https://open.er-api.com/v6/latest")
public interface ExchangeRateClient
{

    @GetMapping("/{baseCurrency}")
    ExchangeRateResponse getExchangeRates(@PathVariable("baseCurrency") String baseCurrency);
}
