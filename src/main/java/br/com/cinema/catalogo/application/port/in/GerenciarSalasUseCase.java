package br.com.cinema.catalogo.application.port.in;

import br.com.cinema.catalogo.domain.model.Sala;

import java.util.List;
import java.util.UUID;

/** Porta de entrada: cadastro e consulta de salas. */
public interface GerenciarSalasUseCase {
    Sala cadastrar(String nome, int capacidade);
    Sala buscar(UUID salaId);
    List<Sala> listar();
}
