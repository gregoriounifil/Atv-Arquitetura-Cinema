package br.com.cinema.shared.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AssentoReservadoEvent(
        UUID reservaId,
        UUID sessaoId,
        UUID assentoId,
        UUID clienteId,
        BigDecimal valor,
        Instant reservadoEm
) {}
