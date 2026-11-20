package br.com.cinema.catalogo.application.port.in;

import br.com.cinema.catalogo.domain.model.Sessao;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Porta de entrada: programar uma sessao nova. */
public interface ProgramarSessaoUseCase {
    Sessao programar(UUID filmeId, UUID salaId, Instant inicio, BigDecimal precoBase);
}
