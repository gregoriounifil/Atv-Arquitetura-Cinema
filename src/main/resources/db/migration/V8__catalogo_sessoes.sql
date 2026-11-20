-- ============================================================================
-- V8__catalogo_sessoes.sql
-- Modulo: catalogo
--
-- Recria a tabela "sessoes" (removida na V7) com identidade UUID.
-- FK para filmes e salas e permitida: sao tabelas do mesmo modulo.
--
-- Regra principal: duas sessoes nao podem se sobrepor na mesma sala. A garantia
-- fica no banco (restricao EXCLUDE), porque uma checagem em Java perderia a
-- corrida entre duas requisicoes simultaneas. O intervalo [inicio, fim) e
-- semiaberto: uma sessao pode comecar exatamente quando a anterior termina.
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE sessoes (
    id          UUID           NOT NULL PRIMARY KEY,
    filme_id    UUID           NOT NULL REFERENCES filmes (id),
    sala_id     UUID           NOT NULL REFERENCES salas (id),
    inicio      TIMESTAMPTZ    NOT NULL,
    fim         TIMESTAMPTZ    NOT NULL,
    preco_base  NUMERIC(10, 2) NOT NULL CHECK (preco_base > 0),
    CONSTRAINT ck_sessoes_fim_apos_inicio CHECK (fim > inicio)
);

ALTER TABLE sessoes ADD CONSTRAINT ex_sessoes_sala_sem_sobreposicao
    EXCLUDE USING gist (sala_id WITH =, tstzrange(inicio, fim) WITH &&);

CREATE INDEX idx_sessoes_filme ON sessoes (filme_id, inicio);
