package org.example.testcurrencyrate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@AutoConfigureMockMvc
@SpringBootTest(
        classes = TestCurrencyRateApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@ContextConfiguration(
        initializers = {BaseFunctionalTest.Initializer.class},
        classes = {BaseFunctionalTest.BaseFunctionalTestConfig.class}
)
public class BaseFunctionalTest
{

    private static PostgreSQLContainer postgreSQLContainer =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                    .withDatabaseName("integration-tests-db")
                    .withUsername("test")
                    .withPassword("test");

    @Autowired
    MockMvc mvc;

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext>
    {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext)
        {
            postgreSQLContainer.start();

            TestPropertyValues.of(
                    "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgreSQLContainer.getUsername(),
                    "spring.datasource.password=" + postgreSQLContainer.getPassword()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    @TestConfiguration
    static class BaseFunctionalTestConfig
    {
        @Bean
        ApplicationListener<ContextClosedEvent> contextClosedEventListener()
        {
            return event -> postgreSQLContainer.stop();
        }

        @Bean
        ObjectMapper objectMapper()
        {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            // You can also configure other settings here if needed
            return objectMapper;
        }
    }
}
