package br.com.cinema.pagamentos.infrastructure.adapter.in.web;

import br.com.cinema.pagamentos.domain.model.MetodoPagamento;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidDadosPagamentoValidator implements ConstraintValidator<ValidDadosPagamento, ProcessarPagamentoRequest> {

    @Override
    public boolean isValid(ProcessarPagamentoRequest request, ConstraintValidatorContext context) {
        if (request == null || request.metodoPagamento() == MetodoPagamento.PIX) {
            return true;
        }

        boolean valido = true;
        context.disableDefaultConstraintViolation();

        valido &= validarCampoObrigatorio(request.numeroCartao(), "numeroCartao", "Numero do cartao e obrigatorio", context);
        valido &= validarCampoObrigatorio(request.nomeTitular(), "nomeTitular", "Nome do titular e obrigatorio", context);
        valido &= validarCampoObrigatorio(request.validade(), "validade", "Validade e obrigatoria", context);
        valido &= validarCampoObrigatorio(request.cvv(), "cvv", "CVV e obrigatorio", context);

        return valido;
    }

    private boolean validarCampoObrigatorio(
            String valor,
            String campo,
            String mensagem,
            ConstraintValidatorContext context
    ) {
        if (valor == null || valor.isBlank()) {
            context.buildConstraintViolationWithTemplate(mensagem)
                    .addPropertyNode(campo)
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
