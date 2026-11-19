package br.com.cinema.catalogo.application.port.out;

import br.com.cinema.catalogo.domain.model.Sala;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Porta de saida: onde as salas ficam guardadas. */
public interface SalaRepositoryPort {
    Sala salvar(Sala sala);
    Optional<Sala> buscarPorId(UUID salaId);
    List<Sala> listarTodas();
}
