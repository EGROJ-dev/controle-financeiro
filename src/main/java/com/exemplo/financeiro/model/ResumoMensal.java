package com.exemplo.financeiro.model;

import java.math.BigDecimal;
import java.time.YearMonth;

public record ResumoMensal(YearMonth mes, BigDecimal receitas, BigDecimal despesas) {

    public BigDecimal saldo() {
        return receitas.subtract(despesas);
    }
}
