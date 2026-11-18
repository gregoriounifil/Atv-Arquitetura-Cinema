package br.com.cinema.reservas.infrastructure.adapter.out.persistence;

import br.com.cinema.reservas.domain.StatusReserva;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reservas")
public class ReservaJpaEntity {

    @Id
    private UUID id;

    @Column(name = "sessao_id", nullable = false)
    private UUID sessaoId;

    @Column(name = "assento_id", nullable = false)
    private UUID assentoId;

    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusReserva status;

    @Column(length = 300)
    private String motivo;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "expira_em", nullable = false)
    private Instant expiraEm;

    protected ReservaJpaEntity() {
        // exigido pelo JPA
    }

    public ReservaJpaEntity(UUID id, UUID sessaoId, UUID assentoId, UUID clienteId, BigDecimal valor,
                            StatusReserva status, String motivo, Instant criadoEm, Instant expiraEm) {
        this.id = id;
        this.sessaoId = sessaoId;
        this.assentoId = assentoId;
        this.clienteId = clienteId;
        this.valor = valor;
        this.status = status;
        this.motivo = motivo;
        this.criadoEm = criadoEm;
        this.expiraEm = expiraEm;
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
