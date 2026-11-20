package br.com.cinema.catalogo.application.service;

import br.com.cinema.catalogo.application.port.in.ConsultarSessoesUseCase;
import br.com.cinema.catalogo.application.port.in.ProgramarSessaoUseCase;
import br.com.cinema.catalogo.application.port.out.FilmeRepositoryPort;
import br.com.cinema.catalogo.application.port.out.PublicarEventoCatalogoPort;
import br.com.cinema.catalogo.application.port.out.SalaRepositoryPort;
import br.com.cinema.catalogo.application.port.out.SessaoRepositoryPort;
import br.com.cinema.catalogo.domain.exception.RecursoNaoEncontradoException;
import br.com.cinema.catalogo.domain.model.Filme;
import br.com.cinema.catalogo.domain.model.Sala;
import br.com.cinema.catalogo.domain.model.Sessao;
import br.com.cinema.shared.events.SessaoCriadaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class SessaoService implements ProgramarSessaoUseCase, ConsultarSessoesUseCase {

    private static final Logger log = LoggerFactory.getLogger(SessaoService.class);

    private final SessaoRepositoryPort sessoes;
    private final FilmeRepositoryPort filmes;
    private final SalaRepositoryPort salas;
    private final PublicarEventoCatalogoPort publicador;

    public SessaoService(SessaoRepositoryPort sessoes, FilmeRepositoryPort filmes,
                         SalaRepositoryPort salas, PublicarEventoCatalogoPort publicador) {
        this.sessoes = sessoes;
        this.filmes = filmes;
        this.salas = salas;
        this.publicador = publicador;
    }

    @Override
    @Transactional
    public Sessao programar(UUID filmeId, UUID salaId, Instant inicio, BigDecimal precoBase) {
        Filme filme = filmes.buscarPorId(filmeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Filme nao encontrado: [%s].".formatted(filmeId)));
        Sala sala = salas.buscarPorId(salaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Sala nao encontrada: [%s].".formatted(salaId)));

        Sessao sessao = sessoes.salvar(Sessao.programar(filme, sala, inicio, precoBase, Instant.now()));

        // O evento so e publicado porque a sessao foi gravada na mesma transacao.
        publicador.publicar(new SessaoCriadaEvent(
                sessao.getId(), sessao.getFilmeId(), sessao.getSalaId(),
                sessao.getInicio(), sessao.getFim(), sessao.getPrecoBase(), sala.getCapacidade()));

        log.info("Sessao programada: id={}, sala={}, inicio={}", sessao.getId(), salaId, sessao.getInicio());
        return sessao;
    }

    @Override
    @Transactional(readOnly = true)
    public Sessao buscar(UUID sessaoId) {
        return sessoes.buscarPorId(sessaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Sessao nao encontrada: [%s].".formatted(sessaoId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sessao> listarPorFilme(UUID filmeId) {
        return sessoes.listarPorFilme(filmeId);
    }
}
