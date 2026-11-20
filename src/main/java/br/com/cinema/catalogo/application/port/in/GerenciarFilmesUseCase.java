package br.com.cinema.catalogo.application.port.in;

import br.com.cinema.catalogo.domain.model.ClassificacaoIndicativa;
import br.com.cinema.catalogo.domain.model.Filme;

import java.util.List;
import java.util.UUID;

/** Porta de entrada: cadastro e consulta de filmes. */
public interface GerenciarFilmesUseCase {
    Filme cadastrar(String titulo, int duracaoMinutos, ClassificacaoIndicativa classificacao);
    Filme buscar(UUID filmeId);
    List<Filme> listar();
}
