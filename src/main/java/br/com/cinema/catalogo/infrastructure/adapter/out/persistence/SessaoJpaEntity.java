package br.com.cinema.catalogo.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sessoes")
public class SessaoJpaEntity {

    @Id
    private UUID id;

    @Column(name = "filme_id", nullable = false)
    private UUID filmeId;

    @Column(name = "sala_id", nullable = false)
    private UUID salaId;

    @Column(nullable = false)
    private Instant inicio;

    @Column(nullable = false)
    private Instant fim;

    @Column(name = "preco_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoBase;

    protected SessaoJpaEntity() {
        // exigido pelo JPA
    }

    public SessaoJpaEntity(UUID id, UUID filmeId, UUID salaId, Instant inicio, Instant fim, BigDecimal precoBase) {
        this.id = id;
        this.filmeId = filmeId;
        this.salaId = salaId;
        this.inicio = inicio;
        this.fim = fim;
        this.precoBase = precoBase;
    }

    public UUID getId() { return id; }
    public UUID getFilmeId() { return filmeId; }
    public UUID getSalaId() { return salaId; }
    public Instant getInicio() { return inicio; }
    public Instant getFim() { return fim; }
    public BigDecimal getPrecoBase() { return precoBase; }
}
