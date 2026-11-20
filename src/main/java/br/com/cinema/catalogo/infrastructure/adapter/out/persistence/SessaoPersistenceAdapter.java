package br.com.cinema.catalogo.infrastructure.adapter.out.persistence;

import br.com.cinema.catalogo.application.port.out.SessaoRepositoryPort;
import br.com.cinema.catalogo.domain.exception.CatalogoDomainException;
import br.com.cinema.catalogo.domain.model.Sessao;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Adaptador de saida: implementa a porta de sessoes usando JPA. */
@Component
public class SessaoPersistenceAdapter implements SessaoRepositoryPort {

    private static final String RESTRICAO_SOBREPOSICAO = "ex_sessoes_sala_sem_sobreposicao";

    private final SpringDataSessaoRepository repository;

    SessaoPersistenceAdapter(SpringDataSessaoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Sessao salvar(Sessao sessao) {
        try {
            return paraDominio(repository.saveAndFlush(new SessaoJpaEntity(
                    sessao.getId(), sessao.getFilmeId(), sessao.getSalaId(),
                    sessao.getInicio(), sessao.getFim(), sessao.getPrecoBase())));
        } catch (DataIntegrityViolationException ex) {
            // Barrado pela restricao EXCLUDE da V8: nem duas requisicoes simultaneas passam.
            String causa = ex.getMostSpecificCause().getMessage();
            if (causa != null && causa.contains(RESTRICAO_SOBREPOSICAO)) {
                throw new CatalogoDomainException("A sala ja tem uma sessao nesse horario.");
            }
            throw ex;
        }
    }

    @Override
    public Optional<Sessao> buscarPorId(UUID sessaoId) {
        return repository.findById(sessaoId).map(SessaoPersistenceAdapter::paraDominio);
    }

    @Override
    public List<Sessao> listarPorFilme(UUID filmeId) {
        return repository.findAllByFilmeIdOrderByInicioAsc(filmeId).stream()
                .map(SessaoPersistenceAdapter::paraDominio)
                .toList();
    }

    private static Sessao paraDominio(SessaoJpaEntity e) {
        return new Sessao(e.getId(), e.getFilmeId(), e.getSalaId(), e.getInicio(), e.getFim(), e.getPrecoBase());
    }
}
