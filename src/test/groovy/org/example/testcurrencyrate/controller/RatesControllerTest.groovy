package org.example.testcurrencyrate.controller


import org.example.testcurrencyrate.service.ExchangeRateCache
import spock.lang.Specification
import spock.lang.Subject

class RatesControllerTest extends Specification {

    @Subject
    RatesController ratesController

    def exchangeRateCache = Mock(ExchangeRateCache)

    def setup() {
        ratesController = new RatesController(exchangeRateCache)
    }


    def "test getCurrencyRates"() {
        given:
            def currency = 'EUR'

        when:
            ratesController.getCurrencyRates(currency)

        then:
            1 * exchangeRateCache.getRates(currency)
            0 * _
    }
}
