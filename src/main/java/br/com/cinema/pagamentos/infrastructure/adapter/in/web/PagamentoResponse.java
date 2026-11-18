package br.com.cinema.pagamentos.infrastructure.adapter.in.web;

import br.com.cinema.pagamentos.application.port.in.PagamentoResult;
import br.com.cinema.pagamentos.domain.model.MetodoPagamento;
import br.com.cinema.pagamentos.domain.model.StatusPagamento;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Resposta detalhada com o estado da cobrança/pagamento")
public record PagamentoResponse(

        @Schema(description = "Identificador único do pagamento", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "Identificador da reserva vinculada", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID reservaId,

        @Schema(description = "Identificador da sessão do cinema", example = "a1b2c3d4-e89b-12d3-a456-426614174001")
        UUID sessaoId,

        @Schema(description = "Identificador do assento reservado", example = "b2c3d4e5-e89b-12d3-a456-426614174002")
        UUID assentoId,

        @Schema(description = "Identificador do cliente comprador", example = "c3d4e5f6-e89b-12d3-a456-426614174003")
        UUID clienteId,

        @Schema(description = "Valor total da cobrança em Reais", example = "45.00")
        BigDecimal valor,

        @Schema(description = "Status atual do pagamento", example = "AGUARDANDO_PAGAMENTO")
        StatusPagamento status,

        @Schema(description = "Método de pagamento utilizado", example = "CARTAO_CREDITO")
        MetodoPagamento metodoPagamento,

        @Schema(description = "Código de autorização retornado pela adquirente", example = "AUTH-4B8F9A12")
        String codigoTransacaoGateway,

        @Schema(description = "Motivo de recusa caso o pagamento não tenha sido aprovado", example = "Saldo insuficiente")
        String motivoRecusa,

        @Schema(description = "Data e hora de criação da cobrança")
        Instant criadoEm,

        @Schema(description = "Data e hora limite de expiração (janela de 10 minutos)")
        Instant expiraEm,

        @Schema(description = "Data e hora da última atualização")
        Instant atualizadoEm,

        @Schema(description = "Segundos restantes até a expiração da cobrança (para contagem regressiva no frontend)", example = "480")
        long segundosRestantesParaExpirar
) {
    public static PagamentoResponse from(PagamentoResult result) {
        Instant agora = Instant.now();
        long segundos = 0;
        if (result.expiraEm() != null && agora.isBefore(result.expiraEm())) {
            segundos = Duration.between(agora, result.expiraEm()).toSeconds();
        }

        return new PagamentoResponse(
                result.id(),
                result.reservaId(),
                result.sessaoId(),
                result.assentoId(),
                result.clienteId(),
                result.valor(),
                result.status(),
                result.metodoPagamento(),
                result.codigoTransacaoGateway(),
                result.motivoRecusa(),
                result.criadoEm(),
                result.expiraEm(),
                result.atualizadoEm(),
                segundos
        );
    }
}
