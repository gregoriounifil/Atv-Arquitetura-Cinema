package br.com.cinema.reservas.domain;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Agregado raiz do modulo Reservas. Java puro: sem Spring, sem JPA.
 *
 * <p>Maquina de estados: PENDENTE -> CONFIRMADA | CANCELADA.
 * As transicoes sao idempotentes para o proprio estado, porque o modulo
 * reage a eventos que podem ser reentregues.</p>
 */
public class Reserva {

    public static final Duration PRAZO_PAGAMENTO = Duration.ofMinutes(10);

    private final UUID id;
    private final UUID sessaoId;
    private final UUID assentoId;
    private final UUID clienteId;
    private final BigDecimal valor;
    private StatusReserva status;
    private String motivo;
    private final Instant criadoEm;
    private final Instant expiraEm;

    public Reserva(UUID id, UUID sessaoId, UUID assentoId, UUID clienteId, BigDecimal valor,
                   StatusReserva status, String motivo, Instant criadoEm, Instant expiraEm) {
        this.id = Objects.requireNonNull(id, "id e obrigatorio");
        this.sessaoId = Objects.requireNonNull(sessaoId, "sessaoId e obrigatorio");
        this.assentoId = Objects.requireNonNull(assentoId, "assentoId e obrigatorio");
        this.clienteId = Objects.requireNonNull(clienteId, "clienteId e obrigatorio");
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReservaDomainException("O valor da reserva deve ser maior que zero.");
        }
        this.valor = valor;
        this.status = Objects.requireNonNull(status, "status e obrigatorio");
        this.motivo = motivo;
        this.criadoEm = Objects.requireNonNull(criadoEm, "criadoEm e obrigatorio");
        this.expiraEm = Objects.requireNonNull(expiraEm, "expiraEm e obrigatorio");
    }

    /** Abre uma reserva pendente com janela de 10 minutos para o pagamento. */
    public static Reserva abrir(UUID sessaoId, UUID assentoId, UUID clienteId,
                                BigDecimal valor, Instant agora) {
        return new Reserva(UUID.randomUUID(), sessaoId, assentoId, clienteId, valor,
                StatusReserva.PENDENTE, null, agora, agora.plus(PRAZO_PAGAMENTO));
    }

    /** Reacao ao PagamentoAprovadoEvent. */
    public void confirmar() {
        if (this.status == StatusReserva.CONFIRMADA) {
            return; // evento reentregue
        }
        exigirPendente("CONFIRMAR");
        this.status = StatusReserva.CONFIRMADA;
        this.motivo = null;
    }

    /** Reacao ao PagamentoRecusadoEvent e ao PagamentoExpiradoEvent. */
    public void cancelar(String motivo) {
        if (this.status == StatusReserva.CANCELADA) {
            return; // evento reentregue
        }
        exigirPendente("CANCELAR");
        this.status = StatusReserva.CANCELADA;
        this.motivo = motivo;
    }

    private void exigirPendente(String operacao) {
        if (this.status.isTerminal()) {
            throw new ReservaDomainException(
                    "Reserva em status [%s] nao permite a operacao [%s].".formatted(this.status, operacao));
        }
    }

    public UUID getId() { return id; }
    public UUID getSessaoId() { return sessaoId; }
    public UUID getAssentoId() { return assentoId; }
    public UUID getClienteId() { return clienteId; }
    public BigDecimal getValor() { return valor; }
    public StatusReserva getStatus() { return status; }
    public String getMotivo() { return motivo; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getExpiraEm() { return expiraEm; }
}
