package br.com.cinema.pagamentos.domain.exception;

public class TransicaoEstadoPagamentoInvalidaException extends PagamentoDomainException {
    public TransicaoEstadoPagamentoInvalidaException(String estadoAtual, String operacaoTentada) {
        super("Transicao de estado invalida: pagamento em status [%s] nao permite a operacao [%s]."
                .formatted(estadoAtual, operacaoTentada));
    }
}
