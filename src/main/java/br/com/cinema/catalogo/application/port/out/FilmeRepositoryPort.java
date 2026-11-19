package br.com.cinema.catalogo.application.port.out;

import br.com.cinema.catalogo.domain.model.Filme;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Porta de saida: onde os filmes ficam guardados. */
public interface FilmeRepositoryPort {
    Filme salvar(Filme filme);
    Optional<Filme> buscarPorId(UUID filmeId);
    List<Filme> listarTodos();
}
