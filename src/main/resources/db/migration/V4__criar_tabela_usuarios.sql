-- ==============================================================================
-- V4__criar_tabela_usuarios.sql
-- Módulo: Autenticação e Usuários (Login e Cadastro de Clientes)
-- ==============================================================================

CREATE TABLE usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    cpf VARCHAR(14) NOT NULL,
    telefone VARCHAR(20),
    perfil VARCHAR(30) DEFAULT 'CLIENTE' NOT NULL,
    ativo BOOLEAN DEFAULT TRUE NOT NULL,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    atualizado_em TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT uk_usuarios_email UNIQUE (email),
    CONSTRAINT uk_usuarios_cpf UNIQUE (cpf),
    CONSTRAINT chk_usuarios_perfil CHECK (perfil IN ('CLIENTE', 'ADMIN', 'FUNCIONARIO'))
);

CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_usuarios_cpf ON usuarios(cpf);
CREATE INDEX idx_usuarios_perfil ON usuarios(perfil);
