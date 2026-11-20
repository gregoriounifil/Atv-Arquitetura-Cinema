package br.com.cinema.catalogo.domain.exception;

/** O filme, a sala ou a sessao procurada nao existe no catalogo. */
public class RecursoNaoEncontradoException extends CatalogoDomainException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
