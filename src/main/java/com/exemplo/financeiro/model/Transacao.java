package com.exemplo.financeiro.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Transacao(int id, String descricao, BigDecimal valor, Tipo tipo,
                        LocalDate data, String categoria) {}
