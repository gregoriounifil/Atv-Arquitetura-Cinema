package br.com.cinema.reservas.application.port.out;

import br.com.cinema.reservas.domain.Reserva;

import java.util.Optional;
import java.util.UUID;

/** Porta de saida: o dominio diz o que precisa, a infraestrutura escolhe como. */
public interface ReservaRepositoryPort {
    Reserva salvar(Reserva reserva);
    Optional<Reserva> buscarPorId(UUID reservaId);
}
