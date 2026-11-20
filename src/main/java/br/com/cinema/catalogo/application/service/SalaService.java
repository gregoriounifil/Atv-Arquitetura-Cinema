package br.com.cinema.catalogo.application.service;

import br.com.cinema.catalogo.application.port.in.GerenciarSalasUseCase;
import br.com.cinema.catalogo.application.port.out.SalaRepositoryPort;
import br.com.cinema.catalogo.domain.exception.RecursoNaoEncontradoException;
import br.com.cinema.catalogo.domain.model.Sala;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SalaService implements GerenciarSalasUseCase {

    private final SalaRepositoryPort repositorio;

    public SalaService(SalaRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    @Transactional
    public Sala cadastrar(String nome, int capacidade) {
        return repositorio.salvar(Sala.nova(nome, capacidade));
    }

    @Override
    @Transactional(readOnly = true)
    public Sala buscar(UUID salaId) {
        return repositorio.buscarPorId(salaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Sala nao encontrada: [%s].".formatted(salaId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sala> listar() {
        return repositorio.listarTodas();
    }
}
