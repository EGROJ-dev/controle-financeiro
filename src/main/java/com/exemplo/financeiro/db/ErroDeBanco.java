package com.exemplo.financeiro.db;

public class ErroDeBanco extends RuntimeException {
    public ErroDeBanco(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
