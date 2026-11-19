-- ============================================================================
-- V7__catalogo_filmes_e_salas.sql
-- Modulo: catalogo
--
-- Substitui as tabelas de catalogo da V1 (BIGSERIAL) por tabelas com
-- identidade UUID, alinhadas com reservas (V6) e pagamentos (V5).
-- A tabela "sessoes" volta na V8, junto com a parte de sessoes do modulo.
-- A tabela "assentos" da V1 nao e usada por nenhum modulo e sai aqui.
-- ============================================================================

DROP TABLE IF EXISTS sessoes;    -- tinha FK para filmes e salas; cai primeiro
DROP TABLE IF EXISTS assentos;   -- tinha FK para salas
DROP TABLE IF EXISTS salas;
DROP TABLE IF EXISTS filmes;

CREATE TABLE filmes (
    id               UUID          NOT NULL PRIMARY KEY,
    titulo           VARCHAR(255)  NOT NULL,
    duracao_minutos  INTEGER       NOT NULL CHECK (duracao_minutos BETWEEN 1 AND 600),
    classificacao    VARCHAR(20)   NOT NULL CHECK (classificacao IN (
        'LIVRE', 'DEZ_ANOS', 'DOZE_ANOS', 'QUATORZE_ANOS', 'DEZESSEIS_ANOS', 'DEZOITO_ANOS'))
);

CREATE TABLE salas (
    id          UUID          NOT NULL PRIMARY KEY,
    nome        VARCHAR(100)  NOT NULL,
    capacidade  INTEGER       NOT NULL CHECK (capacidade BETWEEN 1 AND 500),
    CONSTRAINT uk_salas_nome UNIQUE (nome)
);
