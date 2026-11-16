package br.com.cinema.pagamentos.application.port.in;

public interface ProcessarPagamentoUseCase {
    PagamentoResult executar(ProcessarPagamentoCommand command);
}
