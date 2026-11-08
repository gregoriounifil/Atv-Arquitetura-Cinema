-- ==============================================================================
-- V3__atualizar_colunas_event_publication.sql
-- Atualiza as colunas da tabela event_publication para compatibilidade com Spring Modulith 2.1+
-- ==============================================================================

ALTER TABLE event_publication
    ADD COLUMN IF NOT EXISTS status VARCHAR(50),
    ADD COLUMN IF NOT EXISTS completion_attempts INT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS last_resubmission_date TIMESTAMP WITH TIME ZONE;
