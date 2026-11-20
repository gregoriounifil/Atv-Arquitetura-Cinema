package br.com.cinema.catalogo.infrastructure.adapter.out.persistence;

import br.com.cinema.catalogo.application.port.out.SalaRepositoryPort;
import br.com.cinema.catalogo.domain.exception.CatalogoDomainException;
import br.com.cinema.catalogo.domain.model.Sala;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Adaptador de saida: implementa a porta de salas usando JPA. */
@Component
public class SalaPersistenceAdapter implements SalaRepositoryPort {

    private final SpringDataSalaRepository repository;

    SalaPersistenceAdapter(SpringDataSalaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Sala salvar(Sala sala) {
        try {
            return paraDominio(repository.saveAndFlush(
                    new SalaJpaEntity(sala.getId(), sala.getNome(), sala.getCapacidade())));
        } catch (DataIntegrityViolationException ex) {
            // Barrado pelo indice unico de nome da V7.
            throw new CatalogoDomainException("Ja existe uma sala com o nome [%s].".formatted(sala.getNome()));
        }
    }

    @Override
    public Optional<Sala> buscarPorId(UUID salaId) {
        return repository.findById(salaId).map(SalaPersistenceAdapter::paraDominio);
    }

    @Override
    public List<Sala> listarTodas() {
        return repository.findAllByOrderByNomeAsc().stream()
                .map(SalaPersistenceAdapter::paraDominio)
                .toList();
    }

    private static Sala paraDominio(SalaJpaEntity e) {
        return new Sala(e.getId(), e.getNome(), e.getCapacidade());
    }
}
