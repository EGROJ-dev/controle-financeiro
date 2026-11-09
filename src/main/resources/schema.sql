CREATE TABLE IF NOT EXISTS categoria (
    id   INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(60) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS transacao (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    descricao      VARCHAR(120)  NOT NULL,
    valor          DECIMAL(12,2) NOT NULL CHECK (valor > 0),
    tipo           VARCHAR(10)   NOT NULL CHECK (tipo IN ('RECEITA', 'DESPESA')),
    data_transacao DATE          NOT NULL,
    categoria_id   INT           NOT NULL,
    FOREIGN KEY (categoria_id) REFERENCES categoria(id)
);
