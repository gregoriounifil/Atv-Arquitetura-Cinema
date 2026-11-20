package br.com.cinema.catalogo.infrastructure.adapter.in.web;

import br.com.cinema.catalogo.domain.exception.CatalogoDomainException;
import br.com.cinema.catalogo.domain.exception.RecursoNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduz as excecoes do dominio do catalogo em respostas HTTP (RFC 7807). */
@RestControllerAdvice(basePackages = "br.com.cinema.catalogo")
public class CatalogoExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    ProblemDetail naoEncontrado(RecursoNaoEncontradoException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problema.setTitle("Recurso nao encontrado");
        return problema;
    }

    @ExceptionHandler(CatalogoDomainException.class)
    ProblemDetail regraDeNegocio(CatalogoDomainException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problema.setTitle("Regra de negocio violada");
        return problema;
    }
}
