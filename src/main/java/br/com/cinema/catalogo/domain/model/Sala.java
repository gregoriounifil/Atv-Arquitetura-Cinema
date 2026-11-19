package br.com.cinema.catalogo.domain.model;

import br.com.cinema.catalogo.domain.exception.CatalogoDomainException;

import java.util.Objects;
import java.util.UUID;

/** Sala de exibicao. Java puro: sem Spring, sem JPA. */
public class Sala {

    public static final int CAPACIDADE_MAXIMA = 500;

    private final UUID id;
    private final String nome;
    private final int capacidade;

    public Sala(UUID id, String nome, int capacidade) {
        this.id = Objects.requireNonNull(id, "id e obrigatorio");
        if (nome == null || nome.isBlank()) {
            throw new CatalogoDomainException("O nome da sala e obrigatorio.");
        }
        if (capacidade <= 0 || capacidade > CAPACIDADE_MAXIMA) {
            throw new CatalogoDomainException(
                    "A capacidade da sala deve estar entre 1 e %d lugares.".formatted(CAPACIDADE_MAXIMA));
        }
        this.nome = nome.strip();
        this.capacidade = capacidade;
    }

    public static Sala nova(String nome, int capacidade) {
        return new Sala(UUID.randomUUID(), nome, capacidade);
    }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public int getCapacidade() { return capacidade; }
}
