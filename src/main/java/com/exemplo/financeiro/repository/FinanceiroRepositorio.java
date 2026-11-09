package com.exemplo.financeiro.repository;

import com.exemplo.financeiro.db.ErroDeBanco;
import com.exemplo.financeiro.model.ResumoMensal;
import com.exemplo.financeiro.model.Tipo;
import com.exemplo.financeiro.model.TotalPorCategoria;
import com.exemplo.financeiro.model.Transacao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/** Todas as consultas SQL do sistema ficam aqui. */
public class FinanceiroRepositorio {

    private final Connection conexao;

    public FinanceiroRepositorio(Connection conexao) {
        this.conexao = conexao;
    }

    /** Devolve o id da categoria, criando-a se ainda não existir (sem diferenciar maiúsculas). */
    public int buscarOuCriarCategoria(String nome) {
        try (PreparedStatement busca = conexao.prepareStatement(
                "SELECT id FROM categoria WHERE LOWER(nome) = LOWER(?)")) {
            busca.setString(1, nome);
            try (ResultSet rs = busca.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            try (PreparedStatement insere = conexao.prepareStatement(
                    "INSERT INTO categoria (nome) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                insere.setString(1, nome);
                insere.executeUpdate();
                try (ResultSet chaves = insere.getGeneratedKeys()) {
                    chaves.next();
                    return chaves.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new ErroDeBanco("Erro ao salvar categoria.", e);
        }
    }

    public int inserir(String descricao, BigDecimal valor, Tipo tipo, LocalDate data, int categoriaId) {
        String sql = """
                INSERT INTO transacao (descricao, valor, tipo, data_transacao, categoria_id)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, descricao);
            ps.setBigDecimal(2, valor);
            ps.setString(3, tipo.name());
            ps.setObject(4, data);
            ps.setInt(5, categoriaId);
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                chaves.next();
                return chaves.getInt(1);
            }
        } catch (SQLException e) {
            throw new ErroDeBanco("Erro ao inserir transação.", e);
        }
    }

    public List<Transacao> listarPorMes(YearMonth mes) {
        String sql = """
                SELECT t.id, t.descricao, t.valor, t.tipo, t.data_transacao, c.nome
                FROM transacao t
                JOIN categoria c ON c.id = t.categoria_id
                WHERE t.data_transacao >= ? AND t.data_transacao < ?
                ORDER BY t.data_transacao, t.id
                """;
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setObject(1, mes.atDay(1));
            ps.setObject(2, mes.plusMonths(1).atDay(1));
            try (ResultSet rs = ps.executeQuery()) {
                List<Transacao> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(new Transacao(rs.getInt("id"), rs.getString("descricao"),
                            rs.getBigDecimal("valor"), Tipo.valueOf(rs.getString("tipo")),
                            rs.getObject("data_transacao", LocalDate.class), rs.getString("nome")));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw new ErroDeBanco("Erro ao listar transações.", e);
        }
    }

    /** SUM + CASE: soma receitas e despesas do mês numa única consulta. */
    public ResumoMensal resumo(YearMonth mes) {
        String sql = """
                SELECT
                    COALESCE(SUM(CASE WHEN tipo = 'RECEITA' THEN valor ELSE 0 END), 0) AS receitas,
                    COALESCE(SUM(CASE WHEN tipo = 'DESPESA' THEN valor ELSE 0 END), 0) AS despesas
                FROM transacao
                WHERE data_transacao >= ? AND data_transacao < ?
                """;
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setObject(1, mes.atDay(1));
            ps.setObject(2, mes.plusMonths(1).atDay(1));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new ResumoMensal(mes, rs.getBigDecimal("receitas"), rs.getBigDecimal("despesas"));
            }
        } catch (SQLException e) {
            throw new ErroDeBanco("Erro ao calcular resumo.", e);
        }
    }

    /** GROUP BY: quanto foi gasto em cada categoria, da maior para a menor. */
    public List<TotalPorCategoria> despesasPorCategoria(YearMonth mes) {
        String sql = """
                SELECT c.nome, SUM(t.valor) AS total
                FROM transacao t
                JOIN categoria c ON c.id = t.categoria_id
                WHERE t.tipo = 'DESPESA'
                  AND t.data_transacao >= ? AND t.data_transacao < ?
                GROUP BY c.nome
                ORDER BY total DESC, c.nome
                """;
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setObject(1, mes.atDay(1));
            ps.setObject(2, mes.plusMonths(1).atDay(1));
            try (ResultSet rs = ps.executeQuery()) {
                List<TotalPorCategoria> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(new TotalPorCategoria(rs.getString("nome"), rs.getBigDecimal("total")));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw new ErroDeBanco("Erro ao agrupar despesas.", e);
        }
    }
}
