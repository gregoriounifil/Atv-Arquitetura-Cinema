package br.com.cinema.catalogo.domain.model;

import br.com.cinema.catalogo.domain.exception.CatalogoDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Regras de dominio de Sessao")
class SessaoTest {

    private static final Instant AGORA = Instant.parse("2030-01-01T12:00:00Z");

    private final Filme filme = Filme.novo("Duna", 155, ClassificacaoIndicativa.QUATORZE_ANOS);
    private final Sala sala = Sala.nova("Sala 1", 120);

    @Test
    @DisplayName("O fim da sessao e o inicio mais a duracao do filme")
    void fimEhInicioMaisDuracao() {
        Instant inicio = AGORA.plus(Duration.ofHours(3));

        Sessao sessao = Sessao.programar(filme, sala, inicio, new BigDecimal("32.50"), AGORA);

        assertThat(sessao.getFim()).isEqualTo(inicio.plus(Duration.ofMinutes(155)));
        assertThat(sessao.getFilmeId()).isEqualTo(filme.getId());
        assertThat(sessao.getSalaId()).isEqualTo(sala.getId());
        assertThat(sessao.getPrecoBase()).isEqualByComparingTo("32.50");
    }

    @Test
    @DisplayName("Inicio no passado (ou agora) e recusado")
    void recusaInicioNoPassado() {
        assertThatThrownBy(() -> Sessao.programar(filme, sala, AGORA.minusSeconds(1), BigDecimal.TEN, AGORA))
                .isInstanceOf(CatalogoDomainException.class)
                .hasMessageContaining("futuro");
        assertThatThrownBy(() -> Sessao.programar(filme, sala, AGORA, BigDecimal.TEN, AGORA))
                .isInstanceOf(CatalogoDomainException.class);
    }

    @Test
    @DisplayName("Preco zero, negativo ou ausente e recusado")
    void recusaPrecoInvalido() {
        Instant inicio = AGORA.plusSeconds(3600);

        assertThatThrownBy(() -> Sessao.programar(filme, sala, inicio, BigDecimal.ZERO, AGORA))
                .isInstanceOf(CatalogoDomainException.class);
        assertThatThrownBy(() -> Sessao.programar(filme, sala, inicio, new BigDecimal("-1"), AGORA))
                .isInstanceOf(CatalogoDomainException.class);
        assertThatThrownBy(() -> Sessao.programar(filme, sala, inicio, null, AGORA))
                .isInstanceOf(CatalogoDomainException.class);
    }
}
