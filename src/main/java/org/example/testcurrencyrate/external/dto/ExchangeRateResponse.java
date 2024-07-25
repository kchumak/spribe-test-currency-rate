package org.example.testcurrencyrate.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections4.MapUtils;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateResponse
{
    private static final String SUCCESS = "success";

    private String result;
    @JsonProperty("base_code")
    private String baseCurrency;
    private Map<String, BigDecimal> rates;

    public boolean isSuccessful()
    {
        return SUCCESS.equals(result);
    }

    public boolean isNoRatesInResponse()
    {
        return MapUtils.isEmpty(rates);
    }
}
