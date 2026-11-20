package br.com.cinema.shared.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Publicado pelo catalogo quando uma sessao e programada. Consumido por quem precisa do mapa de assentos. */
public record SessaoCriadaEvent(
        UUID sessaoId,
        UUID filmeId,
        UUID salaId,
        Instant inicio,
        Instant fim,
        BigDecimal precoBase,
        int capacidadeSala
) {}
