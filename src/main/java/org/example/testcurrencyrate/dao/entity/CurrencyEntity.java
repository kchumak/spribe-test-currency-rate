package org.example.testcurrencyrate.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "TB_CURRENCY")
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyEntity
{
    @Id
    @Column(name = "currency_code", nullable = false)
    private String currencyCode;
}
