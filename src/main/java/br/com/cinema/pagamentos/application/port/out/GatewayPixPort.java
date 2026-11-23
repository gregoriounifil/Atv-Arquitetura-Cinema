package br.com.cinema.pagamentos.application.port.out;

import java.math.BigDecimal;
import java.util.UUID;

public interface GatewayPixPort {
    RespostaGateway processarPix(UUID pagamentoId, BigDecimal valor);
}
