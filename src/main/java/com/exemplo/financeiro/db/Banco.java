package com.exemplo.financeiro.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Cria conexões com o H2 e garante que as tabelas existam. */
public class Banco {

    private final String url;

    private Banco(String url) {
        this.url = url;
        criarTabelas();
    }

    /** Banco gravado em disco (cria arquivos com esse prefixo). */
    public static Banco emArquivo(Path arquivoBase) {
        String caminho = arquivoBase.toAbsolutePath().toString().replace('\\', '/');
        return new Banco("jdbc:h2:file:" + caminho);
    }

    /** Banco só em memória, usado nos testes. */
    public static Banco emMemoria(String nome) {
        return new Banco("jdbc:h2:mem:" + nome + ";DB_CLOSE_DELAY=-1");
    }

    public Connection abrir() {
        try {
            return DriverManager.getConnection(url, "sa", "");
        } catch (SQLException e) {
            throw new ErroDeBanco("Não foi possível abrir o banco.", e);
        }
    }

    private void criarTabelas() {
        try (Connection conexao = abrir(); Statement st = conexao.createStatement()) {
            for (String comando : lerSchema().split(";")) {
                if (!comando.isBlank()) {
                    st.execute(comando);
                }
            }
        } catch (SQLException e) {
            throw new ErroDeBanco("Erro ao criar as tabelas.", e);
        }
    }

    private String lerSchema() {
        try (InputStream in = Banco.class.getResourceAsStream("/schema.sql")) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ErroDeBanco("Não foi possível ler o schema.sql.", e);
        }
    }
}
