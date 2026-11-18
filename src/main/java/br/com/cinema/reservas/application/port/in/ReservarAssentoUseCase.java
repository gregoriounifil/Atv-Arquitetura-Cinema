package br.com.cinema.reservas.application.port.in;

import br.com.cinema.reservas.domain.Reserva;

import java.math.BigDecimal;
import java.util.UUID;

/** Porta de entrada acionada pela web. */
public interface ReservarAssentoUseCase {
    Reserva reservar(UUID sessaoId, UUID assentoId, UUID clienteId, BigDecimal valor);
    Reserva consultar(UUID reservaId);
}
