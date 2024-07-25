package org.example.testcurrencyrate.dao.repository;

import org.example.testcurrencyrate.dao.entity.ExchangeRateEntity;
import org.springframework.data.repository.CrudRepository;

public interface ExchangeRateRepository extends CrudRepository<ExchangeRateEntity, String>
{
}
