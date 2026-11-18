package br.com.cinema.shared.events;

import java.time.Instant;
import java.util.UUID;

public record PagamentoRecusadoEvent(
        UUID pagamentoId,
        UUID reservaId,
        UUID sessaoId,
        UUID assentoId,
        String motivo,
        Instant recusadoEm
) {}
