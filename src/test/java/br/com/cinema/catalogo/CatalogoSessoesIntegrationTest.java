package br.com.cinema.catalogo;

import br.com.cinema.catalogo.application.port.in.ConsultarSessoesUseCase;
import br.com.cinema.catalogo.application.port.in.GerenciarFilmesUseCase;
import br.com.cinema.catalogo.application.port.in.GerenciarSalasUseCase;
import br.com.cinema.catalogo.application.port.in.ProgramarSessaoUseCase;
import br.com.cinema.catalogo.domain.exception.CatalogoDomainException;
import br.com.cinema.catalogo.domain.exception.RecursoNaoEncontradoException;
import br.com.cinema.catalogo.domain.model.ClassificacaoIndicativa;
import br.com.cinema.catalogo.domain.model.Filme;
import br.com.cinema.catalogo.domain.model.Sala;
import br.com.cinema.catalogo.domain.model.Sessao;
import br.com.cinema.shared.events.SessaoCriadaEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@RecordApplicationEvents
@DisplayName("Catalogo - sessoes (do caso de uso ate o Postgres)")
class CatalogoSessoesIntegrationTest {

    private static final BigDecimal PRECO = new BigDecimal("30.00");

    @Autowired
    private GerenciarFilmesUseCase filmes;

    @Autowired
    private GerenciarSalasUseCase salas;

    @Autowired
    private ProgramarSessaoUseCase programar;

    @Autowired
    private ConsultarSessoesUseCase consultar;

    @Autowired
    private ApplicationEvents eventos;

    private Filme novoFilme() {
        return filmes.cadastrar("Filme " + UUID.randomUUID(), 120, ClassificacaoIndicativa.LIVRE);
    }

    private Sala novaSala() {
        return salas.cadastrar("Sala " + UUID.randomUUID(), 90);
    }

    /** Horario bem no futuro, para nunca cair no passado. */
    private static Instant amanha() {
        return Instant.now().plus(Duration.ofDays(1));
    }

    @Test
    @DisplayName("Programar uma sessao a grava e publica exatamente um SessaoCriadaEvent")
    void programaEPublicaEvento() {
        Filme filme = novoFilme();
        Sala sala = novaSala();
        Instant inicio = amanha();

        Sessao sessao = programar.programar(filme.getId(), sala.getId(), inicio, PRECO);

        assertThat(sessao.getFim()).isEqualTo(inicio.plus(Duration.ofMinutes(120)));
        assertThat(consultar.buscar(sessao.getId()).getPrecoBase()).isEqualByComparingTo(PRECO);
        assertThat(consultar.listarPorFilme(filme.getId())).extracting(Sessao::getId).containsExactly(sessao.getId());

        var publicados = eventos.stream(SessaoCriadaEvent.class)
                .filter(e -> e.sessaoId().equals(sessao.getId()))
                .toList();
        assertThat(publicados).hasSize(1);
        SessaoCriadaEvent evento = publicados.get(0);
        assertThat(evento.filmeId()).isEqualTo(filme.getId());
        assertThat(evento.salaId()).isEqualTo(sala.getId());
        assertThat(evento.capacidadeSala()).isEqualTo(90);
        assertThat(evento.fim()).isEqualTo(sessao.getFim());
    }

    @Test
    @DisplayName("Sessao que se sobrepoe a outra na mesma sala e recusada, sem publicar evento")
    void recusaSobreposicaoNaMesmaSala() {
        Filme filme = novoFilme();
        Sala sala = novaSala();
        Instant inicio = amanha();
        programar.programar(filme.getId(), sala.getId(), inicio, PRECO);
        long antes = eventos.stream(SessaoCriadaEvent.class).count();

        // comeca 30 min depois do inicio da primeira (que dura 120 min)
        assertThatThrownBy(() -> programar.programar(
                filme.getId(), sala.getId(), inicio.plus(Duration.ofMinutes(30)), PRECO))
                .isInstanceOf(CatalogoDomainException.class)
                .hasMessage("A sala ja tem uma sessao nesse horario.");

        assertThat(eventos.stream(SessaoCriadaEvent.class).count()).isEqualTo(antes);
    }

    @Test
    @DisplayName("Sessao que comeca exatamente quando a anterior termina e aceita")
    void aceitaSessaoColadaNaAnterior() {
        Filme filme = novoFilme();
        Sala sala = novaSala();
        Instant inicio = amanha();
        Sessao primeira = programar.programar(filme.getId(), sala.getId(), inicio, PRECO);

        Sessao segunda = programar.programar(filme.getId(), sala.getId(), primeira.getFim(), PRECO);

        assertThat(segunda.getInicio()).isEqualTo(primeira.getFim());
    }

    @Test
    @DisplayName("A mesma faixa de horario em outra sala e aceita")
    void aceitaMesmoHorarioEmOutraSala() {
        Filme filme = novoFilme();
        Instant inicio = amanha();
        programar.programar(filme.getId(), novaSala().getId(), inicio, PRECO);

        Sessao emOutraSala = programar.programar(filme.getId(), novaSala().getId(), inicio, PRECO);

        assertThat(emOutraSala.getInicio()).isEqualTo(inicio);
    }

    @Test
    @DisplayName("Filme ou sala inexistente lanca RecursoNaoEncontradoException")
    void recusaFilmeOuSalaInexistente() {
        Filme filme = novoFilme();
        Sala sala = novaSala();

        assertThatThrownBy(() -> programar.programar(UUID.randomUUID(), sala.getId(), amanha(), PRECO))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        assertThatThrownBy(() -> programar.programar(filme.getId(), UUID.randomUUID(), amanha(), PRECO))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        assertThatThrownBy(() -> consultar.buscar(UUID.randomUUID()))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
