package br.com.cinema.catalogo.infrastructure.adapter.out.persistence;

import br.com.cinema.catalogo.domain.model.ClassificacaoIndicativa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "filmes")
public class FilmeJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 255)
    private String titulo;

    @Column(name = "duracao_minutos", nullable = false)
    private int duracaoMinutos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClassificacaoIndicativa classificacao;

    protected FilmeJpaEntity() {
        // exigido pelo JPA
    }

    public FilmeJpaEntity(UUID id, String titulo, int duracaoMinutos, ClassificacaoIndicativa classificacao) {
        this.id = id;
        this.titulo = titulo;
        this.duracaoMinutos = duracaoMinutos;
        this.classificacao = classificacao;
    }

    public UUID getId() { return id; }
    public String getTitulo() { return titulo; }
    public int getDuracaoMinutos() { return duracaoMinutos; }
    public ClassificacaoIndicativa getClassificacao() { return classificacao; }
}
