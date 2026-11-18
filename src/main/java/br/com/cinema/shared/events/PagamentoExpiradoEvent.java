package br.com.cinema.shared.events;

import java.time.Instant;
import java.util.UUID;

public record PagamentoExpiradoEvent(
        UUID pagamentoId,
        UUID reservaId,
        UUID sessaoId,
        UUID assentoId,
        Instant expiradoEm
) {}
