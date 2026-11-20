package br.com.cinema.reservas.application.service;

import br.com.cinema.reservas.application.port.in.DesfechoPagamentoUseCase;
import br.com.cinema.reservas.application.port.in.ReservarAssentoUseCase;
import br.com.cinema.reservas.application.port.out.PublicarEventoReservaPort;
import br.com.cinema.reservas.application.port.out.ReservaRepositoryPort;
import br.com.cinema.reservas.domain.Reserva;
import br.com.cinema.reservas.domain.ReservaDomainException;
import br.com.cinema.shared.events.AssentoReservadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class ReservaService implements ReservarAssentoUseCase, DesfechoPagamentoUseCase {

    private static final Logger log = LoggerFactory.getLogger(ReservaService.class);

    private final ReservaRepositoryPort repositorio;
    private final PublicarEventoReservaPort publicador;

    public ReservaService(ReservaRepositoryPort repositorio, PublicarEventoReservaPort publicador) {
        this.repositorio = repositorio;
        this.publicador = publicador;
    }

    @Override
    @Transactional
    public Reserva reservar(UUID sessaoId, UUID assentoId, UUID clienteId, BigDecimal valor) {
        Reserva reserva = repositorio.salvar(
                Reserva.abrir(sessaoId, assentoId, clienteId, valor, Instant.now()));

        // O evento so e publicado porque a reserva foi gravada na mesma transacao.
        publicador.publicar(new AssentoReservadoEvent(
                reserva.getId(), reserva.getSessaoId(), reserva.getAssentoId(),
                reserva.getClienteId(), reserva.getValor(), reserva.getCriadoEm()));

        log.info("Reserva aberta: id={}, assento={}, expiraEm={}",
                reserva.getId(), reserva.getAssentoId(), reserva.getExpiraEm());
        return reserva;
    }

    @Override
    @Transactional(readOnly = true)
    public Reserva consultar(UUID reservaId) {
        return repositorio.buscarPorId(reservaId)
                .orElseThrow(() -> new ReservaDomainException(
                        "Reserva nao encontrada: [%s].".formatted(reservaId)));
    }

    @Override
    @Transactional
    public void confirmar(UUID reservaId) {
        Reserva reserva = consultar(reservaId);
        reserva.confirmar();
        repositorio.salvar(reserva);
        log.info("Reserva CONFIRMADA: id={}", reservaId);
    }

    @Override
    @Transactional
    public void cancelar(UUID reservaId, String motivo) {
        Reserva reserva = consultar(reservaId);
        reserva.cancelar(motivo);
        repositorio.salvar(reserva);
        log.info("Reserva CANCELADA: id={}, motivo={}", reservaId, motivo);
    }
}
