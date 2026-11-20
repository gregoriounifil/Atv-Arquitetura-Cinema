package br.com.cinema.catalogo.application.port.in;

import br.com.cinema.catalogo.domain.model.Sessao;

import java.util.List;
import java.util.UUID;

/** Porta de entrada: consulta de sessoes. */
public interface ConsultarSessoesUseCase {
    Sessao buscar(UUID sessaoId);
    List<Sessao> listarPorFilme(UUID filmeId);
}
