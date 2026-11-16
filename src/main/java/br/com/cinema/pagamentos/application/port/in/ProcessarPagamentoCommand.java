package br.com.cinema.pagamentos.application.port.in;

import br.com.cinema.pagamentos.domain.model.MetodoPagamento;

import java.util.UUID;

public record ProcessarPagamentoCommand(
        UUID pagamentoId,
        MetodoPagamento metodoPagamento,
        String numeroCartao,
        String nomeTitular,
        String validade,
        String cvv
) {}
