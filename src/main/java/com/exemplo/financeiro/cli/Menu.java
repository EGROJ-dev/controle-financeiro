package com.exemplo.financeiro.cli;

import com.exemplo.financeiro.model.ResumoMensal;
import com.exemplo.financeiro.model.Tipo;
import com.exemplo.financeiro.model.TotalPorCategoria;
import com.exemplo.financeiro.model.Transacao;
import com.exemplo.financeiro.service.FinanceiroService;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Menu {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter MES = DateTimeFormatter.ofPattern("MM/yyyy");

    private final FinanceiroService service;
    private final Scanner entrada = new Scanner(System.in);

    public Menu(FinanceiroService service) {
        this.service = service;
    }

    public void executar() {
        boolean rodando = true;
        while (rodando) {
            System.out.println("""

                    ===== Controle Financeiro =====
                    1) Adicionar receita
                    2) Adicionar despesa
                    3) Listar transações do mês
                    4) Resumo do mês
                    5) Exportar mês para CSV
                    0) Sair""");
            System.out.print("> ");
            try {
                switch (entrada.nextLine().trim()) {
                    case "1" -> adicionar(Tipo.RECEITA);
                    case "2" -> adicionar(Tipo.DESPESA);
                    case "3" -> listar(lerMes());
                    case "4" -> resumo(lerMes());
                    case "5" -> exportar(lerMes());
                    case "0" -> rodando = false;
                    default -> System.out.println("Opção inválida.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Atenção: " + e.getMessage());
            }
        }
        System.out.println("Até mais!");
    }

    private void adicionar(Tipo tipo) {
        System.out.print("Descrição: ");
        String descricao = entrada.nextLine();
        System.out.print("Valor (ex.: 49,90): ");
        BigDecimal valor = lerValor(entrada.nextLine());
        System.out.print("Categoria: ");
        String categoria = entrada.nextLine();
        System.out.print("Data (dd/MM/yyyy, vazio para hoje): ");
        String textoData = entrada.nextLine().trim();
        LocalDate data = null;
        if (!textoData.isEmpty()) {
            try {
                data = LocalDate.parse(textoData, DATA);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Data inválida. Use dd/MM/yyyy.");
            }
        }
        int id = service.adicionar(tipo, descricao, valor, data, categoria);
        System.out.println(tipo + " #" + id + " registrada.");
    }

    private void listar(YearMonth mes) {
        List<Transacao> lista = service.listarDoMes(mes);
        if (lista.isEmpty()) {
            System.out.println("Nenhuma transação nesse mês.");
            return;
        }
        for (Transacao t : lista) {
            String sinal = t.tipo() == Tipo.RECEITA ? "+" : "-";
            System.out.printf("%s  %s R$ %s  %s (%s)%n",
                    t.data().format(DATA), sinal, formatar(t.valor()), t.descricao(), t.categoria());
        }
    }

    private void resumo(YearMonth mes) {
        ResumoMensal r = service.resumo(mes);
        System.out.println("Resumo de " + mes.format(MES));
        System.out.println("  Receitas: R$ " + formatar(r.receitas()));
        System.out.println("  Despesas: R$ " + formatar(r.despesas()));
        System.out.println("  Saldo:    R$ " + formatar(r.saldo()));
        List<TotalPorCategoria> porCategoria = service.despesasPorCategoria(mes);
        if (!porCategoria.isEmpty()) {
            System.out.println("Despesas por categoria:");
            for (TotalPorCategoria c : porCategoria) {
                System.out.println("  " + c.categoria() + ": R$ " + formatar(c.total()));
            }
        }
    }

    private void exportar(YearMonth mes) {
        Path arquivo = Path.of("relatorio-" + mes + ".csv");
        service.exportarCsv(mes, arquivo);
        System.out.println("Arquivo gerado: " + arquivo.toAbsolutePath());
    }

    private YearMonth lerMes() {
        System.out.print("Mês (MM/yyyy, vazio para o mês atual): ");
        String texto = entrada.nextLine().trim();
        if (texto.isEmpty()) {
            return YearMonth.from(service.hoje());
        }
        try {
            return YearMonth.parse(texto, MES);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Mês inválido. Use MM/yyyy.");
        }
    }

    private static BigDecimal lerValor(String texto) {
        try {
            return new BigDecimal(texto.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor inválido.");
        }
    }

    private static String formatar(BigDecimal valor) {
        return valor.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }
}
