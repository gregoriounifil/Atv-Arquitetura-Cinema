package br.com.cinema.reservas;

import br.com.cinema.reservas.application.port.in.ReservarAssentoUseCase;
import br.com.cinema.reservas.domain.Reserva;
import br.com.cinema.reservas.domain.ReservaDomainException;
import br.com.cinema.reservas.domain.StatusReserva;
import br.com.cinema.reservas.infrastructure.adapter.in.messaging.PagamentoEventsListener;
import br.com.cinema.shared.events.PagamentoAprovadoEvent;
import br.com.cinema.shared.events.PagamentoExpiradoEvent;
import br.com.cinema.shared.events.PagamentoRecusadoEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DisplayName("Ciclo Event-Driven da Reserva")
class CicloEventDrivenReservaTest {

    @Autowired
    private ReservarAssentoUseCase useCase;

    @Autowired
    private PagamentoEventsListener listener;

    private Reserva novaReserva() {
        return useCase.reservar(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("32.50"));
    }

    @Test
    @DisplayName("Nasce PENDENTE e vira CONFIRMADA ao receber PagamentoAprovadoEvent")
    void deveConfirmarComPagamentoAprovado() {
        Reserva reserva = novaReserva();
        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.PENDENTE);

        listener.onAprovado(new PagamentoAprovadoEvent(UUID.randomUUID(), reserva.getId(),
                reserva.getSessaoId(), reserva.getAssentoId(), "AUTH-123", Instant.now()));

        assertThat(useCase.consultar(reserva.getId()).getStatus())
                .isEqualTo(StatusReserva.CONFIRMADA);
    }

    @Test
    @DisplayName("Vira CANCELADA ao receber PagamentoRecusadoEvent")
    void deveCancelarComPagamentoRecusado() {
        Reserva reserva = novaReserva();

        listener.onRecusado(new PagamentoRecusadoEvent(UUID.randomUUID(), reserva.getId(),
                reserva.getSessaoId(), reserva.getAssentoId(), "Saldo insuficiente", Instant.now()));

        Reserva atualizada = useCase.consultar(reserva.getId());
        assertThat(atualizada.getStatus()).isEqualTo(StatusReserva.CANCELADA);
        assertThat(atualizada.getMotivo()).contains("Saldo insuficiente");
    }

    @Test
    @DisplayName("Vira CANCELADA ao receber PagamentoExpiradoEvent")
    void deveCancelarComPagamentoExpirado() {
        Reserva reserva = novaReserva();

        listener.onExpirado(new PagamentoExpiradoEvent(UUID.randomUUID(), reserva.getId(),
                reserva.getSessaoId(), reserva.getAssentoId(), Instant.now()));

        assertThat(useCase.consultar(reserva.getId()).getStatus())
                .isEqualTo(StatusReserva.CANCELADA);
    }

    @Test
    @DisplayName("E idempotente quando o mesmo evento e reentregue")
    void deveSerIdempotente() {
        Reserva reserva = novaReserva();
        PagamentoAprovadoEvent evento = new PagamentoAprovadoEvent(UUID.randomUUID(), reserva.getId(),
                reserva.getSessaoId(), reserva.getAssentoId(), "AUTH-123", Instant.now());

        listener.onAprovado(evento);
        listener.onAprovado(evento); // reentrega

        assertThat(useCase.consultar(reserva.getId()).getStatus())
                .isEqualTo(StatusReserva.CONFIRMADA);
    }

    @Test
    @DisplayName("Nao permite dois clientes reservarem o mesmo assento da mesma sessao")
    void naoDevePermitirReservaDuplicada() {
        UUID sessaoId = UUID.randomUUID();
        UUID assentoId = UUID.randomUUID();
        useCase.reservar(sessaoId, assentoId, UUID.randomUUID(), new BigDecimal("32.50"));

        assertThatThrownBy(() -> useCase.reservar(sessaoId, assentoId, UUID.randomUUID(),
                new BigDecimal("32.50")))
                .isInstanceOf(ReservaDomainException.class)
                .hasMessageContaining("ja esta reservado");
    }
}
