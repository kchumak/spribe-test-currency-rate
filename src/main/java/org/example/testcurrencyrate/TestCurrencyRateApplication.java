package org.example.testcurrencyrate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableFeignClients
public class TestCurrencyRateApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(TestCurrencyRateApplication.class, args);
    }
}
