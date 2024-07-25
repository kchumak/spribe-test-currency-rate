package org.example.testcurrencyrate.dao.repository;

import org.example.testcurrencyrate.dao.entity.CurrencyEntity;
import org.springframework.data.repository.CrudRepository;

public interface CurrencyRepository extends CrudRepository<CurrencyEntity, String>
{
}
