package br.com.cinema.pagamentos.infrastructure.adapter.in.web;

import br.com.cinema.pagamentos.domain.exception.PagamentoDomainException;
import br.com.cinema.pagamentos.domain.exception.PagamentoExpiradoException;
import br.com.cinema.pagamentos.domain.exception.PagamentoNaoEncontradoException;
import br.com.cinema.pagamentos.domain.exception.TransicaoEstadoPagamentoInvalidaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "br.com.cinema.pagamentos.infrastructure.adapter.in.web")
public class PagamentoExceptionHandler {

    @ExceptionHandler(PagamentoNaoEncontradoException.class)
    public ResponseEntity<ProblemDetail> tratarNaoEncontrado(PagamentoNaoEncontradoException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Pagamento Não Encontrado");
        problem.setType(URI.create("https://cinema.com.br/erros/pagamento-nao-encontrado"));
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(PagamentoExpiradoException.class)
    public ResponseEntity<ProblemDetail> tratarExpirado(PagamentoExpiradoException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setTitle("Pagamento Expirado");
        problem.setType(URI.create("https://cinema.com.br/erros/pagamento-expirado"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("motivo", "Prazo limite de 10 minutos para pagamento foi excedido.");
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(problem);
    }

    @ExceptionHandler(TransicaoEstadoPagamentoInvalidaException.class)
    public ResponseEntity<ProblemDetail> tratarTransicaoInvalida(TransicaoEstadoPagamentoInvalidaException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Transição de Estado Inválida");
        problem.setType(URI.create("https://cinema.com.br/erros/transicao-invalida"));
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(PagamentoDomainException.class)
    public ResponseEntity<ProblemDetail> tratarRegraDominio(PagamentoDomainException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setTitle("Violação de Regra de Negócio");
        problem.setType(URI.create("https://cinema.com.br/erros/regra-de-negocio"));
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> tratarValidacao(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Um ou mais campos da requisição possuem valores inválidos."
        );
        problem.setTitle("Erro de Validação de Dados");
        problem.setType(URI.create("https://cinema.com.br/erros/validacao-dados"));
        problem.setProperty("timestamp", Instant.now());

        Map<String, String> errosCampos = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errosCampos.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        problem.setProperty("erros", errosCampos);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }
}
