package br.com.cinema.pagamentos.domain.model;

import br.com.cinema.pagamentos.domain.exception.PagamentoDomainException;
import br.com.cinema.pagamentos.domain.exception.PagamentoExpiradoException;
import br.com.cinema.pagamentos.domain.exception.TransicaoEstadoPagamentoInvalidaException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Pagamento {

    public static final Duration PRAZO_EXPIRACAO_PADRAO = Duration.ofMinutes(10);

    private final UUID id;
    private final UUID reservaId;
    private final UUID sessaoId;
    private final UUID assentoId;
    private final UUID clienteId;
    private final BigDecimal valor;
    private final String chaveIdempotencia;
    private StatusPagamento status;
    private MetodoPagamento metodoPagamento;
    private String codigoTransacaoGateway;
    private String motivoRecusa;
    private final Instant criadoEm;
    private final Instant expiraEm;
    private Instant atualizadoEm;
    private Long version;

    // Construtor completo para reconstrução pelo MapStruct / PersistenceAdapter
    public Pagamento(
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
            Instant atualizadoEm,
            Long version
    ) {
        this.id = Objects.requireNonNull(id, "id do pagamento e obrigatorio");
        this.reservaId = Objects.requireNonNull(reservaId, "reservaId e obrigatorio");
        this.sessaoId = Objects.requireNonNull(sessaoId, "sessaoId e obrigatorio");
        this.assentoId = Objects.requireNonNull(assentoId, "assentoId e obrigatorio");
        this.clienteId = Objects.requireNonNull(clienteId, "clienteId e obrigatorio");
        this.valor = validarValorPositivo(valor);
        this.chaveIdempotencia = validarTextoObrigatorio(chaveIdempotencia, "chaveIdempotencia");
        this.status = Objects.requireNonNull(status, "status e obrigatorio");
        this.metodoPagamento = metodoPagamento;
        this.codigoTransacaoGateway = codigoTransacaoGateway;
        this.motivoRecusa = motivoRecusa;
        this.criadoEm = Objects.requireNonNull(criadoEm, "criadoEm e obrigatorio");
        this.expiraEm = Objects.requireNonNull(expiraEm, "expiraEm e obrigatorio");
        this.atualizadoEm = Objects.requireNonNull(atualizadoEm, "atualizadoEm e obrigatorio");
        this.version = version;
    }

    /**
     * Factory Method rico: cria uma cobrança pendente com janela exata de 10 minutos.
     */
    public static Pagamento criarAguardandoPagamento(
            UUID reservaId,
            UUID sessaoId,
            UUID assentoId,
            UUID clienteId,
            BigDecimal valor,
            Instant agora
    ) {
        Objects.requireNonNull(agora, "Instante atual (agora) e obrigatorio");
        return new Pagamento(
                UUID.randomUUID(),
                reservaId,
                sessaoId,
                assentoId,
                clienteId,
                valor,
                "RESERVA-" + reservaId,
                StatusPagamento.AGUARDANDO_PAGAMENTO,
                null,
                null,
                null,
                agora,
                agora.plus(PRAZO_EXPIRACAO_PADRAO),
                agora,
                null
        );
    }

    /**
     * Aprova o pagamento caso ainda esteja AGUARDANDO_PAGAMENTO e dentro da janela de 10 minutos.
     */
    public void aprovar(MetodoPagamento metodo, String codigoGateway, Instant agora) {
        validarEstadoNaoTerminal("APROVAR");
        Objects.requireNonNull(metodo, "Metodo de pagamento e obrigatorio");
        validarTextoObrigatorio(codigoGateway, "codigoTransacaoGateway");
        Objects.requireNonNull(agora, "Instante atual e obrigatorio");

        if (estaVencidoEm(agora)) {
            this.status = StatusPagamento.EXPIRADO;
            this.motivoRecusa = "Prazo de 10 minutos para pagamento expirou antes da confirmacao.";
            this.atualizadoEm = agora;
            throw new PagamentoExpiradoException(this.id, this.expiraEm);
        }

        this.metodoPagamento = metodo;
        this.codigoTransacaoGateway = codigoGateway;
        this.motivoRecusa = null;
        this.status = StatusPagamento.APROVADO;
        this.atualizadoEm = agora;
    }

    /**
     * Recusa o pagamento por negativa do gateway de cartão.
     */
    public void recusar(MetodoPagamento metodo, String motivo, Instant agora) {
        validarEstadoNaoTerminal("RECUSAR");
        Objects.requireNonNull(metodo, "Metodo de pagamento e obrigatorio");
        validarTextoObrigatorio(motivo, "motivoRecusa");
        Objects.requireNonNull(agora, "Instante atual e obrigatorio");

        if (estaVencidoEm(agora)) {
            this.status = StatusPagamento.EXPIRADO;
            this.motivoRecusa = "Prazo de 10 minutos expirou.";
            this.atualizadoEm = agora;
            return;
        }

        this.metodoPagamento = metodo;
        this.motivoRecusa = motivo;
        this.status = StatusPagamento.RECUSADO;
        this.atualizadoEm = agora;
    }

    /**
     * Marca o pagamento como EXPIRADO quando o timeout de 10 minutos é atingido.
     */
    public void expirar(Instant agora) {
        if (this.status == StatusPagamento.EXPIRADO) {
            return; // Idempotente para o próprio estado EXPIRADO
        }
        validarEstadoNaoTerminal("EXPIRAR");
        Objects.requireNonNull(agora, "Instante atual e obrigatorio");

        if (!estaVencidoEm(agora)) {
            throw new PagamentoDomainException(
                    "Nao e permitido expirar o pagamento [%s] antes do horario limite [%s]."
                            .formatted(this.id, this.expiraEm));
        }

        this.status = StatusPagamento.EXPIRADO;
        this.motivoRecusa = "Expirado automaticamente por timeout de 10 minutos.";
        this.atualizadoEm = agora;
    }

    public boolean estaVencidoEm(Instant instanteReferencia) {
        return !instanteReferencia.isBefore(this.expiraEm);
    }

    private void validarEstadoNaoTerminal(String operacao) {
        if (this.status.isTerminal()) {
            throw new TransicaoEstadoPagamentoInvalidaException(this.status.name(), operacao);
        }
    }

    private static BigDecimal validarValorPositivo(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PagamentoDomainException("O valor do pagamento deve ser estritamente maior que zero.");
        }
        return valor.setScale(2, RoundingMode.HALF_EVEN);
    }

    private static String validarTextoObrigatorio(String valor, String nomeCampo) {
        if (valor == null || valor.isBlank()) {
            throw new PagamentoDomainException("O campo [" + nomeCampo + "] e obrigatorio.");
        }
        return valor.trim();
    }

    // Getters puros
    public UUID getId() { return id; }
    public UUID getReservaId() { return reservaId; }
    public UUID getSessaoId() { return sessaoId; }
    public UUID getAssentoId() { return assentoId; }
    public UUID getClienteId() { return clienteId; }
    public BigDecimal getValor() { return valor; }
    public String getChaveIdempotencia() { return chaveIdempotencia; }
    public StatusPagamento getStatus() { return status; }
    public MetodoPagamento getMetodoPagamento() { return metodoPagamento; }
    public String getCodigoTransacaoGateway() { return codigoTransacaoGateway; }
    public String getMotivoRecusa() { return motivoRecusa; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getExpiraEm() { return expiraEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
    public Long getVersion() { return version; }
}
