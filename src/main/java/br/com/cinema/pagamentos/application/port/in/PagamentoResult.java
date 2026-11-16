package br.com.cinema.pagamentos.application.port.in;

import br.com.cinema.pagamentos.domain.model.MetodoPagamento;
import br.com.cinema.pagamentos.domain.model.Pagamento;
import br.com.cinema.pagamentos.domain.model.StatusPagamento;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PagamentoResult(
        UUID id,
        UUID reservaId,
        UUID sessaoId,
        UUID assentoId,
        UUID clienteId,
        BigDecimal valor,
        String chaveIdempotencia,
        StatusPagamento status,
        MetodoPagamento metodoPagamento,
        String codigoTransacaoGateway,
        String motivoRecusa,
        Instant criadoEm,
        Instant expiraEm,
        Instant atualizadoEm
) {
    public static PagamentoResult fromDomain(Pagamento p) {
        return new PagamentoResult(
                p.getId(),
                p.getReservaId(),
                p.getSessaoId(),
                p.getAssentoId(),
                p.getClienteId(),
                p.getValor(),
                p.getChaveIdempotencia(),
                p.getStatus(),
                p.getMetodoPagamento(),
                p.getCodigoTransacaoGateway(),
                p.getMotivoRecusa(),
                p.getCriadoEm(),
                p.getExpiraEm(),
                p.getAtualizadoEm()
        );
    }
}
