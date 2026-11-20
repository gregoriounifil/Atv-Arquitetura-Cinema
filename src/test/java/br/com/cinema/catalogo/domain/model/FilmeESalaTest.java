package br.com.cinema.catalogo.domain.model;

import br.com.cinema.catalogo.domain.exception.CatalogoDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Regras de dominio de Filme e Sala")
class FilmeESalaTest {

    @Test
    @DisplayName("Filme valido expoe a duracao usada para calcular o fim da sessao")
    void filmeValido() {
        Filme filme = Filme.novo("  Duna  ", 155, ClassificacaoIndicativa.QUATORZE_ANOS);

        assertThat(filme.getTitulo()).isEqualTo("Duna");
        assertThat(filme.duracao()).isEqualTo(Duration.ofMinutes(155));
    }

    @Test
    @DisplayName("Filme sem titulo, com duracao invalida ou sem classificacao e recusado")
    void filmeInvalido() {
        assertThatThrownBy(() -> Filme.novo(" ", 100, ClassificacaoIndicativa.LIVRE))
                .isInstanceOf(CatalogoDomainException.class);
        assertThatThrownBy(() -> Filme.novo("Duna", 0, ClassificacaoIndicativa.LIVRE))
                .isInstanceOf(CatalogoDomainException.class);
        assertThatThrownBy(() -> Filme.novo("Duna", 601, ClassificacaoIndicativa.LIVRE))
                .isInstanceOf(CatalogoDomainException.class);
        assertThatThrownBy(() -> Filme.novo("Duna", 100, null))
                .isInstanceOf(CatalogoDomainException.class);
    }

    @Test
    @DisplayName("Sala sem nome ou com capacidade fora do limite e recusada")
    void salaInvalida() {
        assertThat(Sala.nova("Sala 1", 120).getCapacidade()).isEqualTo(120);
        assertThatThrownBy(() -> Sala.nova("", 120)).isInstanceOf(CatalogoDomainException.class);
        assertThatThrownBy(() -> Sala.nova("Sala 1", 0)).isInstanceOf(CatalogoDomainException.class);
        assertThatThrownBy(() -> Sala.nova("Sala 1", 501)).isInstanceOf(CatalogoDomainException.class);
    }
}
