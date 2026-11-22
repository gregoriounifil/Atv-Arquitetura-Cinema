package br.com.cinema.pagamentos.infrastructure.adapter.out.gateway;

import br.com.cinema.pagamentos.application.port.out.GatewayCartaoPort;
import br.com.cinema.pagamentos.application.port.out.RespostaGateway;
import br.com.cinema.pagamentos.domain.model.DadosCartao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class FakePaymentGatewayAdapter implements GatewayCartaoPort {

    private static final Logger log = LoggerFactory.getLogger(FakePaymentGatewayAdapter.class);

    @Override
    public RespostaGateway processarCobranca(UUID pagamentoId, BigDecimal valor, DadosCartao dadosCartao) {
        String numeroLimpo = dadosCartao.numeroCartao().replaceAll("\\s+", "");

        if (numeroLimpo.endsWith("9999")) {
            log.info("[FAKE-GATEWAY] Simulando latencia de rede (1500ms) em Virtual Thread para pagamentoId={}", pagamentoId);
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (numeroLimpo.endsWith("0000")) {
            log.warn("[FAKE-GATEWAY] Transacao RECUSADA para pagamentoId={} (final 0000)", pagamentoId);
            return new RespostaGateway(false, null, "Transacao negada pelo emissor: limite insuficiente.");
        }

        String codigoAutorizacao = "AUTH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("[FAKE-GATEWAY] Transacao APROVADA para pagamentoId={}, auth={}", pagamentoId, codigoAutorizacao);
        return new RespostaGateway(true, codigoAutorizacao, null);
    }

    @Override
    public RespostaGateway processarPix(UUID pagamentoId, BigDecimal valor) {
        String codigoPix = "PIX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("[FAKE-GATEWAY] PIX confirmado para pagamentoId={}, valor={}, codigo={}", pagamentoId, valor, codigoPix);
        return new RespostaGateway(true, codigoPix, null);
    }
}
