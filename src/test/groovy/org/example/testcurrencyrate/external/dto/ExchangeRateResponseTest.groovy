package org.example.testcurrencyrate.external.dto

import spock.lang.Specification

class ExchangeRateResponseTest extends Specification {


    def "test isSuccessful"() {
        given:
            def response = new ExchangeRateResponse()
            response.result = result

        when:
            def isSuccessful = response.isSuccessful()

        then:
            isSuccessful == isSuccessfulExpected

        where:
            result        | isSuccessfulExpected
            null          | false
            ''            | false
            'some_string' | false
            'success'     | true
    }

    def "test isNoRatesInResponse"() {
        given:
            def response = new ExchangeRateResponse()
            response.rates = rates

        when:
            def isNoRatesInResponse = response.isNoRatesInResponse()

        then:
            isNoRatesInResponse == isNoRatesInResponseExpected

        where:
            rates                   | isNoRatesInResponseExpected
            null                    | true
            [:]                     | true
            ['USD': BigDecimal.ONE] | false
    }
}
