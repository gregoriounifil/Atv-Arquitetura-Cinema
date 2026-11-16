package br.com.cinema.pagamentos.infrastructure.adapter.in.web;

import br.com.cinema.pagamentos.application.port.in.ProcessarPagamentoCommand;
import br.com.cinema.pagamentos.domain.model.MetodoPagamento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

@Schema(description = "Dados para processamento de pagamento via cartão")
public record ProcessarPagamentoRequest(

        @NotNull(message = "Metodo de pagamento e obrigatorio")
        @Schema(description = "Metodo de pagamento", example = "CARTAO_CREDITO")
        MetodoPagamento metodoPagamento,

        @NotBlank(message = "Numero do cartao e obrigatorio")
        @Pattern(regexp = "^[0-9\\s]{13,19}$", message = "Numero do cartao deve conter entre 13 e 19 digitos")
        @Schema(description = "Numero do cartao de credito (com ou sem espacos). Use final 4242 para simular aprovacao, ou final 0000 para recusa.", example = "4111 1111 1111 4242")
        String numeroCartao,

        @NotBlank(message = "Nome do titular e obrigatorio")
        @Schema(description = "Nome do titular impresso no cartao", example = "JOAO V NOVAES")
        String nomeTitular,

        @NotBlank(message = "Validade e obrigatoria")
        @Pattern(regexp = "^(0[1-9]|1[0-2])\\/([0-9]{2}|[0-9]{4})$", message = "Validade deve estar no formato MM/AA ou MM/AAAA")
        @Schema(description = "Validade do cartao no formato MM/AA ou MM/AAAA", example = "12/2030")
        String validade,

        @NotBlank(message = "CVV e obrigatorio")
        @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV deve ter 3 ou 4 digitos numericos")
        @Schema(description = "Codigo de seguranca do cartao", example = "123")
        String cvv
) {
    public ProcessarPagamentoCommand toCommand(UUID pagamentoId) {
        return new ProcessarPagamentoCommand(
                pagamentoId,
                metodoPagamento,
                numeroCartao,
                nomeTitular,
                validade,
                cvv
        );
    }
}
