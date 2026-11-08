package br.com.cinema.pagamentos.domain.exception;

public class PagamentoDomainException extends RuntimeException {
    public PagamentoDomainException(String message) {
        super(message);
    }
}
