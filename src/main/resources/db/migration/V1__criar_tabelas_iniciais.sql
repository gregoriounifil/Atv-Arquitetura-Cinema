-- ==============================================================================
-- V1__criar_tabelas_iniciais.sql
-- Estrutura inicial do banco de dados para a API de Cinema
-- Módulos: Catálogo, Reservas e Pagamentos
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- 1. MÓDULO CATÁLOGO (Filmes, Salas, Assentos e Sessões)
-- ------------------------------------------------------------------------------

CREATE TABLE filmes (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    sinopse TEXT,
    genero VARCHAR(100),
    duracao_minutos INTEGER NOT NULL,
    classificacao_indicativa VARCHAR(10) NOT NULL,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE salas (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    capacidade INTEGER NOT NULL,
    tipo VARCHAR(50) DEFAULT 'PADRAO' NOT NULL, -- Ex: 2D, 3D, IMAX, VIP
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE assentos (
    id BIGSERIAL PRIMARY KEY,
    sala_id BIGINT NOT NULL REFERENCES salas(id) ON DELETE CASCADE,
    codigo VARCHAR(10) NOT NULL, -- Ex: 'A1', 'A2', 'B1'
    tipo VARCHAR(50) DEFAULT 'PADRAO' NOT NULL, -- Ex: PADRAO, VIP, PCD
    CONSTRAINT uq_sala_assento UNIQUE (sala_id, codigo)
);

CREATE TABLE sessoes (
    id BIGSERIAL PRIMARY KEY,
    filme_id BIGINT NOT NULL REFERENCES filmes(id),
    sala_id BIGINT NOT NULL REFERENCES salas(id),
    data_hora_inicio TIMESTAMP WITH TIME ZONE NOT NULL,
    data_hora_fim TIMESTAMP WITH TIME ZONE NOT NULL,
    preco_base NUMERIC(10, 2) NOT NULL,
    status VARCHAR(30) DEFAULT 'DISPONIVEL' NOT NULL, -- Ex: DISPONIVEL, ESGOTADA, ENCERRADA, CANCELADA
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_sessoes_filme ON sessoes(filme_id);
CREATE INDEX idx_sessoes_sala ON sessoes(sala_id);
CREATE INDEX idx_sessoes_data_hora ON sessoes(data_hora_inicio);

-- ------------------------------------------------------------------------------
-- 2. MÓDULO RESERVAS (Reservas e Ingressos/Assentos Reservados)
-- ------------------------------------------------------------------------------

CREATE TABLE reservas (
    id BIGSERIAL PRIMARY KEY,
    sessao_id BIGINT NOT NULL REFERENCES sessoes(id),
    cliente_nome VARCHAR(255) NOT NULL,
    cliente_cpf VARCHAR(14) NOT NULL,
    cliente_email VARCHAR(255) NOT NULL,
    valor_total NUMERIC(10, 2) NOT NULL,
    status VARCHAR(30) DEFAULT 'PENDENTE' NOT NULL, -- Ex: PENDENTE, CONFIRMADA, CANCELADA, EXPIRADA
    expira_em TIMESTAMP WITH TIME ZONE NOT NULL,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_reservas_sessao ON reservas(sessao_id);
CREATE INDEX idx_reservas_cliente_cpf ON reservas(cliente_cpf);

CREATE TABLE ingressos (
    id BIGSERIAL PRIMARY KEY,
    reserva_id BIGINT NOT NULL REFERENCES reservas(id) ON DELETE CASCADE,
    sessao_id BIGINT NOT NULL REFERENCES sessoes(id),
    assento_id BIGINT NOT NULL REFERENCES assentos(id),
    tipo VARCHAR(30) DEFAULT 'INTEIRA' NOT NULL, -- Ex: INTEIRA, MEIA
    preco NUMERIC(10, 2) NOT NULL,
    -- Garante que o mesmo assento não possa ser vendido 2 vezes para a mesma sessão:
    CONSTRAINT uq_sessao_assento UNIQUE (sessao_id, assento_id)
);

CREATE INDEX idx_ingressos_reserva ON ingressos(reserva_id);

-- ------------------------------------------------------------------------------
-- 3. MÓDULO PAGAMENTOS (Transações e Status de Pagamento)
-- ------------------------------------------------------------------------------

CREATE TABLE pagamentos (
    id BIGSERIAL PRIMARY KEY,
    reserva_id BIGINT NOT NULL UNIQUE REFERENCES reservas(id),
    valor NUMERIC(10, 2) NOT NULL,
    metodo_pagamento VARCHAR(50) NOT NULL, -- Ex: PIX, CARTAO_CREDITO, CARTAO_DEBITO
    status VARCHAR(30) DEFAULT 'PENDENTE' NOT NULL, -- Ex: PENDENTE, APROVADO, RECUSADO, ESTORNADO
    transacao_externa_id VARCHAR(100),
    data_pagamento TIMESTAMP WITH TIME ZONE,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_pagamentos_status ON pagamentos(status);
