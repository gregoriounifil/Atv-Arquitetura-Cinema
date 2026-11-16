package br.com.cinema.pagamentos.application.port.in;

import java.util.UUID;

public interface ConsultarPagamentoUseCase {
    PagamentoResult buscarPorId(UUID pagamentoId);
    PagamentoResult buscarPorReservaId(UUID reservaId);
}
