package br.com.cinema.pagamentos.application.port.in;

public interface CriarCobrancaReservaUseCase {
    PagamentoResult executar(CriarCobrancaCommand command);
}
