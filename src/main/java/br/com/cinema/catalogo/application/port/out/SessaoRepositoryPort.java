package br.com.cinema.catalogo.application.port.out;

import br.com.cinema.catalogo.domain.model.Sessao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Porta de saida: onde as sessoes ficam guardadas. */
public interface SessaoRepositoryPort {
    Sessao salvar(Sessao sessao);
    Optional<Sessao> buscarPorId(UUID sessaoId);
    List<Sessao> listarPorFilme(UUID filmeId);
}
