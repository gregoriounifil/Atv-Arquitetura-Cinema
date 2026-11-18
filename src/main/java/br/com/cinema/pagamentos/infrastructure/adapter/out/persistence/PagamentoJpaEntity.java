package br.com.cinema.pagamentos.infrastructure.adapter.out.persistence;

import br.com.cinema.pagamentos.domain.model.MetodoPagamento;
import br.com.cinema.pagamentos.domain.model.StatusPagamento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pagamentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagamentoJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "reserva_id", nullable = false, unique = true, updatable = false)
    private UUID reservaId;

    @Column(name = "sessao_id", nullable = false, updatable = false)
    private UUID sessaoId;

    @Column(name = "assento_id", nullable = false, updatable = false)
    private UUID assentoId;

    @Column(name = "cliente_id", nullable = false, updatable = false)
    private UUID clienteId;

    @Column(name = "valor", precision = 10, scale = 2, nullable = false, updatable = false)
    private BigDecimal valor;

    @Column(name = "chave_idempotencia", length = 100, nullable = false, unique = true, updatable = false)
    private String chaveIdempotencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private StatusPagamento status;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pagamento", length = 30)
    private MetodoPagamento metodoPagamento;

    @Column(name = "codigo_transacao_gateway", length = 120)
    private String codigoTransacaoGateway;

    @Column(name = "motivo_recusa", length = 500)
    private String motivoRecusa;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "expira_em", nullable = false, updatable = false)
    private Instant expiraEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
