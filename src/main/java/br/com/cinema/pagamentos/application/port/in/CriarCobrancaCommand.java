package br.com.cinema.pagamentos.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record CriarCobrancaCommand(
        UUID reservaId,
        UUID sessaoId,
        UUID assentoId,
        UUID clienteId,
        BigDecimal valor
) {}
