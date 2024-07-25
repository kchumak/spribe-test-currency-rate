package org.example.testcurrencyrate.service


import spock.lang.Specification
import spock.lang.Subject

class ExchangeRatesSchedulerTest extends Specification {

    @Subject
    ExchangeRatesScheduler scheduler

    def exchangeRateCache = Mock(ExchangeRateCache)

    def setup() {
        scheduler = new ExchangeRatesScheduler(exchangeRateCache)
    }

    def "test warm up cache"() {
        when:
            scheduler.getRates()

        then:
            1 * exchangeRateCache.refreshRates()
            0 * _
    }
}
