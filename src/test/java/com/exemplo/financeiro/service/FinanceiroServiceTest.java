package com.exemplo.financeiro.service;

import com.exemplo.financeiro.db.Banco;
import com.exemplo.financeiro.model.ResumoMensal;
import com.exemplo.financeiro.model.Tipo;
import com.exemplo.financeiro.model.TotalPorCategoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FinanceiroServiceTest {

    private static final YearMonth OUTUBRO = YearMonth.of(2026, 10);

    private FinanceiroService service;

    @BeforeEach
    void preparar() {
        Clock relogio = Clock.fixed(Instant.parse("2026-10-30T12:00:00Z"), ZoneOffset.UTC);
        Banco banco = Banco.emMemoria("teste" + UUID.randomUUID().toString().replace("-", ""));
        service = new FinanceiroService(banco, relogio);
    }

    private static BigDecimal v(String texto) {
        return new BigDecimal(texto);
    }

    @Test
    void resumoSomaReceitasEDespesasDoMes() {
        service.adicionar(Tipo.RECEITA, "Salário", v("3000"), LocalDate.of(2026, 10, 5), "Trabalho");
        service.adicionar(Tipo.DESPESA, "Aluguel", v("1200.50"), LocalDate.of(2026, 10, 6), "Moradia");
        service.adicionar(Tipo.DESPESA, "Mercado", v("350.25"), LocalDate.of(2026, 10, 10), "Alimentação");

        ResumoMensal resumo = service.resumo(OUTUBRO);

        assertEquals(0, v("3000").compareTo(resumo.receitas()));
        assertEquals(0, v("1550.75").compareTo(resumo.despesas()));
        assertEquals(0, v("1449.25").compareTo(resumo.saldo()));
    }

    @Test
    void agrupaDespesasPorCategoriaDaMaiorParaAMenor() {
        service.adicionar(Tipo.DESPESA, "Mercado", v("200"), LocalDate.of(2026, 10, 2), "Alimentação");
        service.adicionar(Tipo.DESPESA, "Restaurante", v("100"), LocalDate.of(2026, 10, 3), "alimentação");
        service.adicionar(Tipo.DESPESA, "Ônibus", v("150"), LocalDate.of(2026, 10, 4), "Transporte");
        service.adicionar(Tipo.RECEITA, "Freela", v("900"), LocalDate.of(2026, 10, 4), "Trabalho");

        List<TotalPorCategoria> totais = service.despesasPorCategoria(OUTUBRO);

        assertEquals(2, totais.size());
        assertEquals("Alimentação", totais.get(0).categoria());
        assertEquals(0, v("300").compareTo(totais.get(0).total()));
        assertEquals("Transporte", totais.get(1).categoria());
    }

    @Test
    void listaSomenteTransacoesDoMesPedido() {
        service.adicionar(Tipo.DESPESA, "Setembro", v("10"), LocalDate.of(2026, 9, 30), "Teste");
        service.adicionar(Tipo.DESPESA, "Outubro", v("20"), LocalDate.of(2026, 10, 1), "Teste");
        service.adicionar(Tipo.DESPESA, "Novembro", v("30"), LocalDate.of(2026, 11, 1), "Teste");

        var lista = service.listarDoMes(OUTUBRO);

        assertEquals(1, lista.size());
        assertEquals("Outubro", lista.get(0).descricao());
    }

    @Test
    void usaADataDeHojeQuandoNaoInformada() {
        service.adicionar(Tipo.RECEITA, "Presente", v("50"), null, "Extra");
        assertEquals(LocalDate.of(2026, 10, 30), service.listarDoMes(OUTUBRO).get(0).data());
    }

    @Test
    void mesSemMovimentoTemResumoZerado() {
        ResumoMensal resumo = service.resumo(YearMonth.of(2020, 1));
        assertEquals(0, BigDecimal.ZERO.compareTo(resumo.saldo()));
    }

    @Test
    void rejeitaValoresEDescricoesInvalidos() {
        assertThrows(IllegalArgumentException.class,
                () -> service.adicionar(Tipo.DESPESA, "Zero", v("0"), null, "X"));
        assertThrows(IllegalArgumentException.class,
                () -> service.adicionar(Tipo.DESPESA, "Negativo", v("-5"), null, "X"));
        assertThrows(IllegalArgumentException.class,
                () -> service.adicionar(Tipo.DESPESA, " ", v("5"), null, "X"));
        assertThrows(IllegalArgumentException.class,
                () -> service.adicionar(Tipo.DESPESA, "Sem categoria", v("5"), null, " "));
    }

    @Test
    void exportaCsvComAspasEVirgulaDecimal(@TempDir Path pasta) throws Exception {
        service.adicionar(Tipo.DESPESA, "Livro \"Java\"", v("49.9"), LocalDate.of(2026, 10, 8), "Estudos");

        Path arquivo = service.exportarCsv(OUTUBRO, pasta.resolve("saida.csv"));
        List<String> linhas = Files.readAllLines(arquivo);

        assertEquals("data;tipo;categoria;descricao;valor", linhas.get(0));
        assertEquals("2026-10-08;DESPESA;\"Estudos\";\"Livro \"\"Java\"\"\";49,90", linhas.get(1));
    }
}
