package com.exemplo.financeiro.service;

import com.exemplo.financeiro.db.Banco;
import com.exemplo.financeiro.db.ErroDeBanco;
import com.exemplo.financeiro.model.ResumoMensal;
import com.exemplo.financeiro.model.Tipo;
import com.exemplo.financeiro.model.TotalPorCategoria;
import com.exemplo.financeiro.model.Transacao;
import com.exemplo.financeiro.repository.FinanceiroRepositorio;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class FinanceiroService {

    private final Banco banco;
    private final Clock relogio;

    public FinanceiroService(Banco banco, Clock relogio) {
        this.banco = banco;
        this.relogio = relogio;
    }

    public LocalDate hoje() {
        return LocalDate.now(relogio);
    }

    public int adicionar(Tipo tipo, String descricao, BigDecimal valor, LocalDate data, String categoria) {
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("A descrição não pode ser vazia.");
        }
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("O valor precisa ser maior que zero.");
        }
        if (categoria == null || categoria.isBlank()) {
            throw new IllegalArgumentException("Informe uma categoria.");
        }
        BigDecimal valorArredondado = valor.setScale(2, RoundingMode.HALF_UP);
        LocalDate dataFinal = data == null ? hoje() : data;

        return emTransacao(repo -> {
            int categoriaId = repo.buscarOuCriarCategoria(categoria.trim());
            return repo.inserir(descricao.trim(), valorArredondado, tipo, dataFinal, categoriaId);
        });
    }

    public List<Transacao> listarDoMes(YearMonth mes) {
        return emTransacao(repo -> repo.listarPorMes(mes));
    }

    public ResumoMensal resumo(YearMonth mes) {
        return emTransacao(repo -> repo.resumo(mes));
    }

    public List<TotalPorCategoria> despesasPorCategoria(YearMonth mes) {
        return emTransacao(repo -> repo.despesasPorCategoria(mes));
    }

    /** Gera um CSV (separado por ponto e vírgula, como o Excel em português espera). */
    public Path exportarCsv(YearMonth mes, Path destino) {
        List<String> linhas = new ArrayList<>();
        linhas.add("data;tipo;categoria;descricao;valor");
        for (Transacao t : listarDoMes(mes)) {
            linhas.add(String.join(";",
                    t.data().toString(), t.tipo().name(), campo(t.categoria()), campo(t.descricao()),
                    t.valor().setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',')));
        }
        try {
            return Files.write(destino, linhas, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível gravar o CSV.", e);
        }
    }

    private static String campo(String texto) {
        return "\"" + texto.replace("\"", "\"\"") + "\"";
    }

    private <T> T emTransacao(Function<FinanceiroRepositorio, T> acao) {
        try (Connection conexao = banco.abrir()) {
            conexao.setAutoCommit(false);
            try {
                T resultado = acao.apply(new FinanceiroRepositorio(conexao));
                conexao.commit();
                return resultado;
            } catch (RuntimeException e) {
                conexao.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new ErroDeBanco("Erro de transação.", e);
        }
    }
}
