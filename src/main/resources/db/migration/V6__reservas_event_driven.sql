-- ============================================================================
-- V6__reservas_event_driven.sql
-- Modulo: reservas (Dupla 2)
--
-- Recria a tabela de reservas com identidade UUID e SEM chave estrangeira
-- para o modulo Catalogo. A V1 tinha criado reservas.sessao_id referenciando
-- sessoes(id), o que acopla dois bounded contexts no nivel do banco. Aqui a
-- referencia passa a ser um UUID logico, igual ao que a V5 fez em pagamentos.
-- ============================================================================

DROP TABLE IF EXISTS ingressos;   -- tinha FK para reservas; cai primeiro
DROP TABLE IF EXISTS reservas;

CREATE TABLE reservas (
    id          UUID           NOT NULL PRIMARY KEY,
    sessao_id   UUID           NOT NULL,
    assento_id  UUID           NOT NULL,
    cliente_id  UUID           NOT NULL,
    valor       NUMERIC(10, 2) NOT NULL CHECK (valor > 0),
    status      VARCHAR(20)    NOT NULL CHECK (status IN ('PENDENTE', 'CONFIRMADA', 'CANCELADA')),
    motivo      VARCHAR(300),
    criado_em   TIMESTAMPTZ    NOT NULL,
    expira_em   TIMESTAMPTZ    NOT NULL
);

-- Invariante do modulo: um assento so pode ter UMA reserva viva por sessao.
-- O indice e PARCIAL de proposito: apos o cancelamento o lugar volta a ser
-- reservavel. Um indice unico comum bloquearia o assento para sempre.
CREATE UNIQUE INDEX uk_reserva_ativa_por_assento
    ON reservas (sessao_id, assento_id)
    WHERE status IN ('PENDENTE', 'CONFIRMADA');

CREATE INDEX idx_reservas_sessao ON reservas (sessao_id);
