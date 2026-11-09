package com.exemplo.financeiro;

import com.exemplo.financeiro.cli.Menu;
import com.exemplo.financeiro.db.Banco;
import com.exemplo.financeiro.service.FinanceiroService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;

public class Main {
    public static void main(String[] args) throws IOException {
        Path pasta = Path.of(args.length > 0 ? args[0] : "dados");
        Files.createDirectories(pasta);

        Banco banco = Banco.emArquivo(pasta.resolve("financeiro"));
        new Menu(new FinanceiroService(banco, Clock.systemDefaultZone())).executar();
    }
}
