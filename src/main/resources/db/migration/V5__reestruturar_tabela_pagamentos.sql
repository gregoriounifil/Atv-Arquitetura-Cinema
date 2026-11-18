-- ============================================================================
-- Migration: V5__reestruturar_tabela_pagamentos.sql
-- Modulo: pagamentos (Dupla 3)
-- Descricao: Reestrutura a tabela de pagamentos com identidade UUID,
--            desacoplamento de FK entre bounded contexts, constraints de
--            idempotencia, controle de concorrencia otimista (@Version) e
--            indices otimizados para o job de expiracao (10 minutos).
-- ============================================================================

DROP TABLE IF EXISTS pagamentos;

CREATE TABLE pagamentos (
    id                        UUID             NOT NULL,
    reserva_id                UUID             NOT NULL,
    sessao_id                 UUID             NOT NULL,
    assento_id                UUID             NOT NULL,
    cliente_id                UUID             NOT NULL,
    valor                     NUMERIC(10, 2)  NOT NULL,
    chave_idempotencia        VARCHAR(100)    NOT NULL,
    status                    VARCHAR(30)     NOT NULL,
    metodo_pagamento          VARCHAR(30)     NULL,
    codigo_transacao_gateway  VARCHAR(120)    NULL,
    motivo_recusa             VARCHAR(500)    NULL,
    criado_em                 TIMESTAMPTZ     NOT NULL,
    expira_em                 TIMESTAMPTZ     NOT NULL,
    atualizado_em             TIMESTAMPTZ     NOT NULL,
    version                   BIGINT          NOT NULL DEFAULT 0,

    CONSTRAINT pk_pagamentos PRIMARY KEY (id),
    CONSTRAINT uk_pagamentos_reserva_id UNIQUE (reserva_id),
    CONSTRAINT uk_pagamentos_chave_idempotencia UNIQUE (chave_idempotencia),
    CONSTRAINT chk_pagamentos_valor_positivo CHECK (valor > 0),
    CONSTRAINT chk_pagamentos_status_valido CHECK (
        status IN ('AGUARDANDO_PAGAMENTO', 'APROVADO', 'RECUSADO', 'EXPIRADO')
    )
);

-- Indice composto para consultas por status e ordenacao temporal (criado_em)
CREATE INDEX idx_pagamentos_status_criado_em
    ON pagamentos (status, criado_em DESC);

-- Indice parcial ultra-otimizado para o Scheduled Job de expiracao (10 minutos):
-- Indexa apenas registros que ainda estao AGUARDANDO_PAGAMENTO!
CREATE INDEX idx_pagamentos_pendentes_expiracao
    ON pagamentos (expira_em ASC, criado_em ASC)
    WHERE status = 'AGUARDANDO_PAGAMENTO';

-- Indice de auditoria por sessao
CREATE INDEX idx_pagamentos_sessao_id
    ON pagamentos (sessao_id);
