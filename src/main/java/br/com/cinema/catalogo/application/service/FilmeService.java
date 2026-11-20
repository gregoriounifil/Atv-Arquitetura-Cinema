package br.com.cinema.catalogo.application.service;

import br.com.cinema.catalogo.application.port.in.GerenciarFilmesUseCase;
import br.com.cinema.catalogo.application.port.out.FilmeRepositoryPort;
import br.com.cinema.catalogo.domain.exception.RecursoNaoEncontradoException;
import br.com.cinema.catalogo.domain.model.ClassificacaoIndicativa;
import br.com.cinema.catalogo.domain.model.Filme;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class FilmeService implements GerenciarFilmesUseCase {

    private final FilmeRepositoryPort repositorio;

    public FilmeService(FilmeRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    @Transactional
    public Filme cadastrar(String titulo, int duracaoMinutos, ClassificacaoIndicativa classificacao) {
        return repositorio.salvar(Filme.novo(titulo, duracaoMinutos, classificacao));
    }

    @Override
    @Transactional(readOnly = true)
    public Filme buscar(UUID filmeId) {
        return repositorio.buscarPorId(filmeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Filme nao encontrado: [%s].".formatted(filmeId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Filme> listar() {
        return repositorio.listarTodos();
    }
}
