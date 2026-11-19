package br.com.cinema.catalogo.domain.model;

import br.com.cinema.catalogo.domain.exception.CatalogoDomainException;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

/** Filme em cartaz. Java puro: sem Spring, sem JPA. */
public class Filme {

    public static final int DURACAO_MAXIMA_MINUTOS = 600;

    private final UUID id;
    private final String titulo;
    private final int duracaoMinutos;
    private final ClassificacaoIndicativa classificacao;

    public Filme(UUID id, String titulo, int duracaoMinutos, ClassificacaoIndicativa classificacao) {
        this.id = Objects.requireNonNull(id, "id e obrigatorio");
        if (titulo == null || titulo.isBlank()) {
            throw new CatalogoDomainException("O titulo do filme e obrigatorio.");
        }
        if (duracaoMinutos <= 0 || duracaoMinutos > DURACAO_MAXIMA_MINUTOS) {
            throw new CatalogoDomainException(
                    "A duracao do filme deve estar entre 1 e %d minutos.".formatted(DURACAO_MAXIMA_MINUTOS));
        }
        if (classificacao == null) {
            throw new CatalogoDomainException("A classificacao indicativa e obrigatoria.");
        }
        this.titulo = titulo.strip();
        this.duracaoMinutos = duracaoMinutos;
        this.classificacao = classificacao;
    }

    public static Filme novo(String titulo, int duracaoMinutos, ClassificacaoIndicativa classificacao) {
        return new Filme(UUID.randomUUID(), titulo, duracaoMinutos, classificacao);
    }

    /** Usado pela sessao para calcular o horario de termino. */
    public Duration duracao() {
        return Duration.ofMinutes(duracaoMinutos);
    }

    public UUID getId() { return id; }
    public String getTitulo() { return titulo; }
    public int getDuracaoMinutos() { return duracaoMinutos; }
    public ClassificacaoIndicativa getClassificacao() { return classificacao; }
}
