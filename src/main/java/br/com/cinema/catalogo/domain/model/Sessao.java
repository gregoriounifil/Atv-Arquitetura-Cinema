package br.com.cinema.catalogo.domain.model;

import br.com.cinema.catalogo.domain.exception.CatalogoDomainException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Sessao: um filme exibido em uma sala num horario. Java puro: sem Spring, sem JPA. */
public class Sessao {

    private final UUID id;
    private final UUID filmeId;
    private final UUID salaId;
    private final Instant inicio;
    private final Instant fim;
    private final BigDecimal precoBase;

    public Sessao(UUID id, UUID filmeId, UUID salaId, Instant inicio, Instant fim, BigDecimal precoBase) {
        this.id = Objects.requireNonNull(id, "id e obrigatorio");
        this.filmeId = Objects.requireNonNull(filmeId, "filmeId e obrigatorio");
        this.salaId = Objects.requireNonNull(salaId, "salaId e obrigatorio");
        this.inicio = Objects.requireNonNull(inicio, "inicio e obrigatorio");
        this.fim = Objects.requireNonNull(fim, "fim e obrigatorio");
        this.precoBase = Objects.requireNonNull(precoBase, "precoBase e obrigatorio");
    }

    /** Programa uma sessao nova. O horario de termino e calculado aqui: inicio + duracao do filme. */
    public static Sessao programar(Filme filme, Sala sala, Instant inicio, BigDecimal precoBase, Instant agora) {
        if (filme == null) {
            throw new CatalogoDomainException("O filme da sessao e obrigatorio.");
        }
        if (sala == null) {
            throw new CatalogoDomainException("A sala da sessao e obrigatoria.");
        }
        if (inicio == null || !inicio.isAfter(agora)) {
            throw new CatalogoDomainException("O inicio da sessao deve ser no futuro.");
        }
        if (precoBase == null || precoBase.signum() <= 0) {
            throw new CatalogoDomainException("O preco base da sessao deve ser maior que zero.");
        }
        return new Sessao(UUID.randomUUID(), filme.getId(), sala.getId(),
                inicio, inicio.plus(filme.duracao()), precoBase);
    }

    public UUID getId() { return id; }
    public UUID getFilmeId() { return filmeId; }
    public UUID getSalaId() { return salaId; }
    public Instant getInicio() { return inicio; }
    public Instant getFim() { return fim; }
    public BigDecimal getPrecoBase() { return precoBase; }
}
