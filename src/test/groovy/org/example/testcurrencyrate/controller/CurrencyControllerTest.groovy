package org.example.testcurrencyrate.controller


import org.example.testcurrencyrate.model.CurrencyDto
import org.example.testcurrencyrate.service.ExchangeRateCache
import spock.lang.Specification
import spock.lang.Subject

class CurrencyControllerTest extends Specification {

    @Subject
    CurrencyController currencyController

    def exchangeRateCache = Mock(ExchangeRateCache)

    def setup() {
        currencyController = new CurrencyController(exchangeRateCache)
    }

    def "test getAllCurrencies"() {
        when:
            currencyController.getAllCurrencies()

        then:
            1 * exchangeRateCache.getCurrencies()
            0 * _
    }

    def "test addCurrency"() {
        given:
            def currency = 'EUR'
            def dto = new CurrencyDto(currency)

        when:
            currencyController.addCurrency(dto)

        then:
            1 * exchangeRateCache.addCurrency(currency)
            0 * _
    }
}
