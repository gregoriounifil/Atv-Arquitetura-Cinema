package br.com.cinema.catalogo.domain.exception;

/** Violacao de uma regra de negocio do catalogo. */
public class CatalogoDomainException extends RuntimeException {

    public CatalogoDomainException(String mensagem) {
        super(mensagem);
    }
}
