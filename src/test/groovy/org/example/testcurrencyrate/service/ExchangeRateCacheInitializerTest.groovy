package org.example.testcurrencyrate.service

import org.springframework.boot.ApplicationArguments
import spock.lang.Specification
import spock.lang.Subject

class ExchangeRateCacheInitializerTest extends Specification {

    @Subject
    ExchangeRateCacheInitializer initializer

    def exchangeRateCache = Mock(ExchangeRateCache)

    def setup() {
        initializer = new ExchangeRateCacheInitializer(exchangeRateCache)
    }

    def "test warm up cache"() {
        given:
            def args = Mock(ApplicationArguments)

        when:
            initializer.run(args)

        then:
            1 * exchangeRateCache.refreshRates()
            0 * _
    }
}
