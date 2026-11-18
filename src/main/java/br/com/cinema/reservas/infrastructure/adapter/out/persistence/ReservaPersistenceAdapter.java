package br.com.cinema.reservas.infrastructure.adapter.out.persistence;

import br.com.cinema.reservas.application.port.out.ReservaRepositoryPort;
import br.com.cinema.reservas.domain.Reserva;
import br.com.cinema.reservas.domain.ReservaDomainException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de saida: implementa a porta usando JPA. Trocar Postgres por
 * outra tecnologia mexe apenas neste arquivo - o dominio nao muda.
 */
@Component
public class ReservaPersistenceAdapter implements ReservaRepositoryPort {

    private final SpringDataReservaRepository repository;

    ReservaPersistenceAdapter(SpringDataReservaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Reserva salvar(Reserva reserva) {
        try {
            return paraDominio(repository.saveAndFlush(paraEntidade(reserva)));
        } catch (DataIntegrityViolationException ex) {
            // Barrado pelo indice unico parcial: o assento ja tem reserva ativa.
            throw new ReservaDomainException(
                    "O assento [%s] ja esta reservado para a sessao [%s]."
                            .formatted(reserva.getAssentoId(), reserva.getSessaoId()));
        }
    }

    @Override
    public Optional<Reserva> buscarPorId(UUID reservaId) {
        return repository.findById(reservaId).map(ReservaPersistenceAdapter::paraDominio);
    }

    private static ReservaJpaEntity paraEntidade(Reserva r) {
        return new ReservaJpaEntity(r.getId(), r.getSessaoId(), r.getAssentoId(), r.getClienteId(),
                r.getValor(), r.getStatus(), r.getMotivo(), r.getCriadoEm(), r.getExpiraEm());
    }

    private static Reserva paraDominio(ReservaJpaEntity e) {
        return new Reserva(e.getId(), e.getSessaoId(), e.getAssentoId(), e.getClienteId(),
                e.getValor(), e.getStatus(), e.getMotivo(), e.getCriadoEm(), e.getExpiraEm());
    }
}
