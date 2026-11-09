# 💰 Controle Financeiro

![CI](https://github.com/EGROJ-dev/controle-financeiro/actions/workflows/ci.yml/badge.svg)
![Java 17](https://img.shields.io/badge/Java-17-orange)
![Licença MIT](https://img.shields.io/badge/licença-MIT-blue)

Controle de receitas e despesas em linha de comando, feito com **Java 17**, **JDBC** e **SQL** (banco H2). Mostra o saldo do mês, quanto foi gasto em cada categoria e exporta um relatório em CSV para abrir no Excel.

Projeto de estudo focado em **consultas SQL de verdade**: agregações, agrupamentos e filtros por período.

## Objetivo

Praticar **consultas SQL de agregação** (`SUM`, `GROUP BY`, filtros por período) e o tratamento correto de valores em dinheiro (`DECIMAL` e `BigDecimal`), gerando relatórios mensais a partir de dados reais.

## Funcionalidades

- Cadastro de receitas e despesas com valor, data e categoria (a categoria é criada na hora, sem diferenciar maiúsculas de minúsculas)
- Lista das transações de um mês
- Resumo do mês: receitas, despesas, saldo e despesas por categoria
- Exportação do mês para CSV (separado por `;`, com vírgula decimal)

## Tecnologias

| Tecnologia | Para que serve no projeto |
|---|---|
| Java 17 | Linguagem do projeto |
| JDBC | Comunicação do Java com o banco de dados |
| H2 | Banco de dados SQL que roda dentro do próprio programa, sem instalação |
| Maven | Compilação e gerenciamento de dependências |
| JUnit 5 | Testes automatizados |
| GitHub Actions | Integração contínua: roda os testes a cada alteração |
| CSV | Formato do relatório exportado, aberto direto no Excel |

## Conceitos praticados

| Conceito | Onde ver |
|---|---|
| `SUM` com `CASE` (receitas e despesas numa só consulta) | `FinanceiroRepositorio.resumo` |
| `GROUP BY` e `ORDER BY` | `FinanceiroRepositorio.despesasPorCategoria` |
| Filtro por período (`>=` e `<`) | `listarPorMes` |
| `DECIMAL` e `BigDecimal` para dinheiro (nunca `double`) | `schema.sql` e `service/` |
| Restrições `CHECK` no banco | `schema.sql` |
| Geração de arquivo CSV com aspas escapadas | `FinanceiroService.exportarCsv` |
| Testes com banco em memória e pasta temporária | `FinanceiroServiceTest` |

## Estrutura

```
src/main/java/com/exemplo/financeiro
├── Main.java
├── cli/Menu.java                       # menu no terminal
├── db/                                 # conexão com o H2 e criação das tabelas
├── model/                              # Tipo, Transacao, ResumoMensal...
├── repository/FinanceiroRepositorio    # todo o SQL fica aqui
└── service/FinanceiroService           # validações, transações e CSV
src/main/resources/schema.sql
```

## Como executar

Requisitos: JDK 17+ e Maven 3.8+.

```bash
git clone https://github.com/EGROJ-dev/controle-financeiro.git
cd controle-financeiro
mvn clean package
java -jar target/controle-financeiro-1.0.0.jar
```

Os dados ficam na pasta `dados/`.

> Se os acentos aparecerem trocados no terminal do Windows, use o Git Bash.

## Exemplo de uso

```
Resumo de 10/2026
  Receitas: R$ 3000,00
  Despesas: R$ 1550,75
  Saldo:    R$ 1449,25
Despesas por categoria:
  Moradia: R$ 1200,50
  Alimentação: R$ 350,25
```

CSV gerado:

```
data;tipo;categoria;descricao;valor
2026-10-05;RECEITA;"Trabalho";"Salário";3000,00
2026-10-06;DESPESA;"Moradia";"Aluguel";1200,50
```

## Testes

```bash
mvn test
```

São 7 testes, cobrindo o saldo, o agrupamento por categoria, o filtro por mês, as validações e o CSV.

## Próximos passos

- [ ] Orçamento mensal por categoria, com aviso ao passar do limite
- [ ] Editar e apagar transações
- [ ] Comparar um mês com o anterior
- [ ] Trocar o H2 pelo MySQL

## Autor

**Jorge Gabriel Cordeiro do Nascimento**
[LinkedIn](https://www.linkedin.com/in/jorge-gabriel-cordeiro-9b25702b) · [GitHub](https://github.com/EGROJ-dev)

## Licença

Distribuído sob a licença MIT. Veja o arquivo [LICENSE](LICENSE).
