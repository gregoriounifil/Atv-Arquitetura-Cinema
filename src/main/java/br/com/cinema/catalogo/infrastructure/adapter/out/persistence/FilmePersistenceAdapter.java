package br.com.cinema.catalogo.infrastructure.adapter.out.persistence;

import br.com.cinema.catalogo.application.port.out.FilmeRepositoryPort;
import br.com.cinema.catalogo.domain.model.Filme;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Adaptador de saida: implementa a porta de filmes usando JPA. */
@Component
public class FilmePersistenceAdapter implements FilmeRepositoryPort {

    private final SpringDataFilmeRepository repository;

    FilmePersistenceAdapter(SpringDataFilmeRepository repository) {
        this.repository = repository;
    }

    @Override
    public Filme salvar(Filme filme) {
        return paraDominio(repository.saveAndFlush(new FilmeJpaEntity(
                filme.getId(), filme.getTitulo(), filme.getDuracaoMinutos(), filme.getClassificacao())));
    }

    @Override
    public Optional<Filme> buscarPorId(UUID filmeId) {
        return repository.findById(filmeId).map(FilmePersistenceAdapter::paraDominio);
    }

    @Override
    public List<Filme> listarTodos() {
        return repository.findAllByOrderByTituloAsc().stream()
                .map(FilmePersistenceAdapter::paraDominio)
                .toList();
    }

    private static Filme paraDominio(FilmeJpaEntity e) {
        return new Filme(e.getId(), e.getTitulo(), e.getDuracaoMinutos(), e.getClassificacao());
    }
}
