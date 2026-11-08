package br.com.cinema.pagamentos.domain.exception;

import java.util.UUID;

public class PagamentoNaoEncontradoException extends PagamentoDomainException {
    public PagamentoNaoEncontradoException(UUID id) {
        super("Pagamento nao encontrado para o identificador [%s].".formatted(id));
    }

    public PagamentoNaoEncontradoException(String message) {
        super(message);
    }
}
