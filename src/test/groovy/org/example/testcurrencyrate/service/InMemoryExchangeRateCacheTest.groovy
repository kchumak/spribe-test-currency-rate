package org.example.testcurrencyrate.service

import org.example.testcurrencyrate.dao.repository.CurrencyRepository
import org.example.testcurrencyrate.dao.repository.ExchangeRateRepository
import org.example.testcurrencyrate.exception.ApplicationException
import org.example.testcurrencyrate.external.client.ExchangeRateClient
import org.example.testcurrencyrate.external.dto.ExchangeRateResponse
import org.example.testcurrencyrate.model.ExchangeRateDto
import org.springframework.transaction.support.TransactionTemplate
import spock.lang.Specification
import spock.lang.Subject

import java.time.Instant
import java.util.concurrent.locks.Lock

class InMemoryExchangeRateCacheTest extends Specification {

    @Subject
    InMemoryExchangeRateCache cache

    def currencyRepository = Mock(CurrencyRepository)
    def exchangeRateRepository = Mock(ExchangeRateRepository)
    def exchangeRateClient = Mock(ExchangeRateClient)
    def transactionTemplate = Mock(TransactionTemplate)
    def lock = Mock(Lock)
    def cacheMap = Mock(Map)

    def setup() {
        cache = new InMemoryExchangeRateCache(
                currencyRepository, exchangeRateRepository, exchangeRateClient, transactionTemplate, lock, cacheMap
        )
    }

    def "test getCurrencies"() {
        given:
            def currencies = Set.of('EUR', 'USD')

        when:
            def result = cache.getCurrencies()

        then:
            1 * cacheMap.keySet() >> currencies
            0 * _
            result == currencies
    }

    def "test addCurrency"() {
        given:
            def currency = 'EUR'
        when:
            cache.addCurrency(currency)

        then:
            1 * cacheMap.containsKey(currency) >> false
            1 * currencyRepository.save(entity -> {
                assert entity.currencyCode == currency
            })
            1 * cacheMap.put(currency, [])
            1 * cacheMap.keySet() >> Set.of('EUR', 'USD')
            1 * lock.lock()
            1 * lock.unlock()
            2 * transactionTemplate.execute(_)
            0 * _
    }

    def "test addCurrency already exists"() {
        given:
            def currency = 'EUR'

        when:
            cache.addCurrency(currency)

        then:
            1 * cacheMap.containsKey(currency) >> true
            0 * _
            thrown(ApplicationException)
    }

    def "test getRates"() {
        given:
            def currency = 'EUR'
            def rates = [
                    new ExchangeRateDto('EUR', BigDecimal.ONE, Instant.now()),
                    new ExchangeRateDto('USD', BigDecimal.TEN, Instant.now())
            ]

        when:
            def result = cache.getRates(currency)

        then:
            1 * cacheMap.get(currency) >> rates
            0 * _
            result == rates
    }

    def "test getRates no currency"() {
        given:
            def currency = 'EUR'

        when:
            def result = cache.getRates(currency)

        then:
            1 * cacheMap.get(currency) >> null
            0 * _
            thrown(ApplicationException)
    }

    def "test obtainRatesForCurrency"() {
        given:
            def currency = 'EUR'
            def response = new ExchangeRateResponse('success', 'EUR', ['EUR': BigDecimal.ONE])

        when:
            def result = cache.obtainRatesForCurrency(currency)

        then:
            1 * exchangeRateClient.getExchangeRates(currency) >> response
            0 * _
            result == response.rates
    }

    def "test obtainRatesForCurrency - wrong response"() {
        given:
            def currency = 'EUR'

        when:
            cache.obtainRatesForCurrency(currency)

        then:
            1 * exchangeRateClient.getExchangeRates(currency) >> response
            0 * _
            thrown(ApplicationException)

        where:
            response << [null, new ExchangeRateResponse('fail', null, null), new ExchangeRateResponse('success', 'EUR', null)]
    }

    def "test refreshRatesForCurrency"() {
        given:
            def currency = 'EUR'
            def availableCurrencies = Set.of('EUR', 'USD')
            def response = new ExchangeRateResponse('success', 'EUR', ['EUR': BigDecimal.ONE, 'USD': BigDecimal.TEN, 'PLN': BigDecimal.TEN])

        when:
            cache.refreshRatesForCurrency(currency, availableCurrencies)

        then:
            1 * exchangeRateClient.getExchangeRates(currency) >> response
            1 * exchangeRateRepository.saveAll(rates -> {
                assert rates.size() == 2
                assert rates[0].baseCurrency == currency
                assert rates[0].targetCurrency == currency
                assert rates[1].baseCurrency == currency
                assert rates[1].targetCurrency == 'USD'
            })
            1 * cacheMap.put(currency, rates -> {
                assert rates.size() == 2
                assert rates[0].targetCurrency == currency
                assert rates[1].targetCurrency == 'USD'
            })
            0 * _
    }

    def "test refreshRates"() {
        given:
            def currencies = Set.of('EUR', 'USD')

        when:
            cache.refreshRates()

        then:
            1 * lock.lock()
            1 * cacheMap.keySet() >> currencies
            currencies.size() * transactionTemplate.execute(_)
            1 * lock.unlock()
            0 * _
    }
}
