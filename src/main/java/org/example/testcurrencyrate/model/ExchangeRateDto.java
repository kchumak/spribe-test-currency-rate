package org.example.testcurrencyrate.model;

import java.math.BigDecimal;
import java.time.Instant;

public record ExchangeRateDto(String targetCurrency, BigDecimal rate, Instant timestamp)
{
}
