package br.com.cinema.catalogo;

import br.com.cinema.catalogo.application.port.in.GerenciarFilmesUseCase;
import br.com.cinema.catalogo.application.port.in.GerenciarSalasUseCase;
import br.com.cinema.catalogo.domain.exception.CatalogoDomainException;
import br.com.cinema.catalogo.domain.exception.RecursoNaoEncontradoException;
import br.com.cinema.catalogo.domain.model.ClassificacaoIndicativa;
import br.com.cinema.catalogo.domain.model.Filme;
import br.com.cinema.catalogo.domain.model.Sala;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DisplayName("Catalogo - cadastro de filmes e salas (do caso de uso ate o Postgres)")
class CatalogoFilmesSalasIntegrationTest {

    @Autowired
    private GerenciarFilmesUseCase filmes;

    @Autowired
    private GerenciarSalasUseCase salas;

    @Test
    @DisplayName("Cadastra um filme e o encontra de volta")
    void deveCadastrarEConsultarFilme() {
        Filme filme = filmes.cadastrar("Interestelar", 169, ClassificacaoIndicativa.DEZ_ANOS);

        Filme encontrado = filmes.buscar(filme.getId());
        assertThat(encontrado.getTitulo()).isEqualTo("Interestelar");
        assertThat(encontrado.getDuracaoMinutos()).isEqualTo(169);
        assertThat(filmes.listar()).extracting(Filme::getId).contains(filme.getId());
    }

    @Test
    @DisplayName("Cadastra uma sala e recusa outra com o mesmo nome")
    void naoDevePermitirSalaComNomeRepetido() {
        String nome = "Sala " + UUID.randomUUID();
        Sala sala = salas.cadastrar(nome, 80);

        assertThat(salas.buscar(sala.getId()).getNome()).isEqualTo(nome);
        assertThatThrownBy(() -> salas.cadastrar(nome, 100))
                .isInstanceOf(CatalogoDomainException.class)
                .hasMessageContaining("Ja existe uma sala");
    }

    @Test
    @DisplayName("Buscar filme ou sala inexistente lanca RecursoNaoEncontradoException")
    void deveAvisarQuandoNaoExiste() {
        assertThatThrownBy(() -> filmes.buscar(UUID.randomUUID()))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        assertThatThrownBy(() -> salas.buscar(UUID.randomUUID()))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
