package org.example.testcurrencyrate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.testcurrencyrate.external.client.ExchangeRateClient;
import org.example.testcurrencyrate.external.dto.ExchangeRateResponse;
import org.example.testcurrencyrate.model.ExchangeRateDto;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
class AddCurrencyFlowFunctionalTest extends BaseFunctionalTest
{
    @MockBean
    private ExchangeRateClient exchangeRateClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void add_currency_flow() throws Exception
    {
        // mock external calls
        // we can skip mocking ExchangeRateClient class and then will be real integration test
        var euroMockedResponse = euroResponse();
        var usdMockedResponse = usdResponse();
        Mockito.when(exchangeRateClient.getExchangeRates("EUR")).thenReturn(euroMockedResponse);
        Mockito.when(exchangeRateClient.getExchangeRates("USD")).thenReturn(usdMockedResponse);

        // first start - no currencies
        mvc.perform(get("/api/public/v1/currencies")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]")) // Check that the content is an empty JSON array
                .andExpect(jsonPath("$").isArray()) // Ensure that it's an array
                .andExpect(jsonPath("$.length()").value(0)); // Ensure that the array is empty

        // add EUR
        mvc.perform(post("/api/public/v1/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currencyCode\": \"EUR\"}"))
                .andExpect(status().isCreated());

        // add USD
        mvc.perform(post("/api/public/v1/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currencyCode\": \"USD\"}"))
                .andExpect(status().isCreated());

        // request second time currencies - both EUR and USD should be there
        var currencies = mvc.perform(get("/api/public/v1/currencies")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(currencies).isEqualTo("[\"EUR\",\"USD\"]");

        // get rates for EUR
        var euroRatesResponse = mvc.perform(get("/api/public/v1/currency-rates/EUR")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var euroRates = json2ExchangeRateMap(euroRatesResponse);
        assertEquals(2, euroRates.size());
        assertTrue(euroRates.containsKey("EUR"));
        assertTrue(euroRates.containsKey("USD"));
        assertEquals(euroMockedResponse.getRates().get("EUR"), euroRates.get("EUR"));
        assertEquals(euroMockedResponse.getRates().get("USD"), euroRates.get("USD"));

        // get rates for USD
        var usdRatesResponse = mvc.perform(get("/api/public/v1/currency-rates/USD")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var usdRates = json2ExchangeRateMap(usdRatesResponse);
        assertEquals(2, usdRates.size());
        assertTrue(usdRates.containsKey("EUR"));
        assertTrue(usdRates.containsKey("USD"));
        assertEquals(usdMockedResponse.getRates().get("EUR"), usdRates.get("EUR"));
        assertEquals(usdMockedResponse.getRates().get("USD"), usdRates.get("USD"));
    }

    private ExchangeRateResponse euroResponse()
    {
        Map<String, BigDecimal> rates = Map.of(
                "EUR", new BigDecimal("1"),
                "AED", new BigDecimal("3.986648"),
                "AFN", new BigDecimal("76.915373"),
                "ALL", new BigDecimal("100.328398"),
                "AMD", new BigDecimal("421.478335"),
                "ANG", new BigDecimal("1.943118"),
                "USD", new BigDecimal("1.085513")
        );
        return new ExchangeRateResponse("success", "EUR", rates);
    }

    private ExchangeRateResponse usdResponse()
    {
        Map<String, BigDecimal> rates = Map.of(
                "USD", new BigDecimal("1"),
                "AED", new BigDecimal("3.6725"),
                "AFN", new BigDecimal("70.708628"),
                "ALL", new BigDecimal("92.228462"),
                "AMD", new BigDecimal("387.767941"),
                "ANG", new BigDecimal("1.79"),
                "EUR", new BigDecimal("0.921224")
        );

        return new ExchangeRateResponse("success", "USD", rates);
    }

    private Map<String, BigDecimal> json2ExchangeRateMap(String response) throws JsonProcessingException
    {
        List<ExchangeRateDto> ratesList = objectMapper.readValue(
                response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, ExchangeRateDto.class)
        );
        return ratesList.stream().collect(Collectors.toMap(ExchangeRateDto::targetCurrency, ExchangeRateDto::rate));
    }
}
