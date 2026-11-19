package br.com.cinema.catalogo.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "salas")
public class SalaJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false)
    private int capacidade;

    protected SalaJpaEntity() {
        // exigido pelo JPA
    }

    public SalaJpaEntity(UUID id, String nome, int capacidade) {
        this.id = id;
        this.nome = nome;
        this.capacidade = capacidade;
    }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public int getCapacidade() { return capacidade; }
}
