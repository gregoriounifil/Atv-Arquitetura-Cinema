package br.com.cinema.pagamentos.infrastructure.adapter.in.web;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = ValidDadosPagamentoValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDadosPagamento {
    String message() default "Dados de pagamento invalidos para o metodo selecionado";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
