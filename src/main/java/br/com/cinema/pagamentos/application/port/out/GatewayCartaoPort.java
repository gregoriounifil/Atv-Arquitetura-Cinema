package br.com.cinema.pagamentos.application.port.out;

import br.com.cinema.pagamentos.domain.model.DadosCartao;

import java.math.BigDecimal;
import java.util.UUID;

public interface GatewayCartaoPort {
    RespostaGateway processarCobranca(UUID pagamentoId, BigDecimal valor, DadosCartao dadosCartao);

    /**
     * Processa uma cobrança instantânea via PIX sem necessidade de dados de cartão.
     */
    RespostaGateway processarPix(UUID pagamentoId, BigDecimal valor);
}
