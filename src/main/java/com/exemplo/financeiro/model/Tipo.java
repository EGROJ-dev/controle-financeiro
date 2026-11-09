package com.exemplo.financeiro.model;

public enum Tipo {
    RECEITA, DESPESA;

    public static Tipo de(String texto) {
        return switch (texto.trim().toLowerCase()) {
            case "1", "receita" -> RECEITA;
            case "2", "despesa" -> DESPESA;
            default -> throw new IllegalArgumentException("Tipo inválido: " + texto);
        };
    }
}
