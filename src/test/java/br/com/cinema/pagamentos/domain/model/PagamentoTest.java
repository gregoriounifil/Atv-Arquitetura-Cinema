package br.com.cinema.pagamentos.domain.model;

import br.com.cinema.pagamentos.domain.exception.PagamentoDomainException;
import br.com.cinema.pagamentos.domain.exception.PagamentoExpiradoException;
import br.com.cinema.pagamentos.domain.exception.TransicaoEstadoPagamentoInvalidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@DisplayName("Testes Unitários do Domínio - Pagamento")
class PagamentoTest {

    private final UUID reservaId = UUID.randomUUID();
    private final UUID sessaoId = UUID.randomUUID();
    private final UUID assentoId = UUID.randomUUID();
    private final UUID clienteId = UUID.randomUUID();
    private final BigDecimal valorPadrao = new BigDecimal("45.00");
    private final Instant agora = Instant.parse("2026-11-10T10:00:00Z");

    private Pagamento criarCobrancaValida() {
        return Pagamento.criarAguardandoPagamento(
                reservaId,
                sessaoId,
                assentoId,
                clienteId,
                valorPadrao,
                agora
        );
    }

    @Nested
    @DisplayName("Criação de Cobrança (Factory Method)")
    class CriacaoCobranca {

        @Test
        @DisplayName("Deve criar cobrança pendente com status AGUARDANDO_PAGAMENTO e prazo de 10 minutos")
        void deveCriarCobrancaComSucesso() {
            Pagamento pagamento = criarCobrancaValida();

            assertThat(pagamento.getId()).isNotNull();
            assertThat(pagamento.getReservaId()).isEqualTo(reservaId);
            assertThat(pagamento.getSessaoId()).isEqualTo(sessaoId);
            assertThat(pagamento.getAssentoId()).isEqualTo(assentoId);
            assertThat(pagamento.getClienteId()).isEqualTo(clienteId);
            assertThat(pagamento.getValor()).isEqualByComparingTo("45.00");
            assertThat(pagamento.getChaveIdempotencia()).isEqualTo("RESERVA-" + reservaId);
            assertThat(pagamento.getStatus()).isEqualTo(StatusPagamento.AGUARDANDO_PAGAMENTO);
            assertThat(pagamento.getCriadoEm()).isEqualTo(agora);
            assertThat(pagamento.getExpiraEm()).isEqualTo(agora.plus(Duration.ofMinutes(10)));
            assertThat(pagamento.getAtualizadoEm()).isEqualTo(agora);
            assertThat(pagamento.getMetodoPagamento()).isNull();
            assertThat(pagamento.getCodigoTransacaoGateway()).isNull();
        }

        @Test
        @DisplayName("Deve lançar exceção se o valor for nulo, zero ou negativo")
        void deveRejeitarValoresInvalidos() {
            assertThatThrownBy(() -> Pagamento.criarAguardandoPagamento(reservaId, sessaoId, assentoId, clienteId, null, agora))
                    .isInstanceOf(PagamentoDomainException.class)
                    .hasMessageContaining("estritamente maior que zero");

            assertThatThrownBy(() -> Pagamento.criarAguardandoPagamento(reservaId, sessaoId, assentoId, clienteId, BigDecimal.ZERO, agora))
                    .isInstanceOf(PagamentoDomainException.class)
                    .hasMessageContaining("estritamente maior que zero");

            assertThatThrownBy(() -> Pagamento.criarAguardandoPagamento(reservaId, sessaoId, assentoId, clienteId, new BigDecimal("-10.00"), agora))
                    .isInstanceOf(PagamentoDomainException.class)
                    .hasMessageContaining("estritamente maior que zero");
        }

        @Test
        @DisplayName("Deve lançar NullPointerException para parâmetros obrigatórios nulos")
        void deveRejeitarCamposObrigatoriosNulos() {
            assertThatThrownBy(() -> Pagamento.criarAguardandoPagamento(null, sessaoId, assentoId, clienteId, valorPadrao, agora))
                    .isInstanceOf(NullPointerException.class);

            assertThatThrownBy(() -> Pagamento.criarAguardandoPagamento(reservaId, sessaoId, assentoId, clienteId, valorPadrao, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Transição: Aprovação")
    class Aprovacao {

        @Test
        @DisplayName("Deve aprovar pagamento dentro da janela de 10 minutos")
        void deveAprovarDentroDoPrazo() {
            Pagamento pagamento = criarCobrancaValida();
            Instant momentoAprovacao = agora.plus(Duration.ofMinutes(5));

            pagamento.aprovar(MetodoPagamento.CARTAO_CREDITO, "AUTH-123456", momentoAprovacao);

            assertThat(pagamento.getStatus()).isEqualTo(StatusPagamento.APROVADO);
            assertThat(pagamento.getMetodoPagamento()).isEqualTo(MetodoPagamento.CARTAO_CREDITO);
            assertThat(pagamento.getCodigoTransacaoGateway()).isEqualTo("AUTH-123456");
            assertThat(pagamento.getAtualizadoEm()).isEqualTo(momentoAprovacao);
            assertThat(pagamento.getMotivoRecusa()).isNull();
        }

        @Test
        @DisplayName("Deve expirar e lançar PagamentoExpiradoException se tentar aprovar após prazo de 10 minutos")
        void deveExpirarSeAprovarAposPrazo() {
            Pagamento pagamento = criarCobrancaValida();
            Instant momentoAposPrazo = agora.plus(Duration.ofMinutes(10)); // Exato instante de expiração

            assertThatThrownBy(() -> pagamento.aprovar(MetodoPagamento.CARTAO_CREDITO, "AUTH-123456", momentoAposPrazo))
                    .isInstanceOf(PagamentoExpiradoException.class);

            assertThat(pagamento.getStatus()).isEqualTo(StatusPagamento.EXPIRADO);
            assertThat(pagamento.getMotivoRecusa()).contains("expirou");
            assertThat(pagamento.getAtualizadoEm()).isEqualTo(momentoAposPrazo);
        }

        @Test
        @DisplayName("Deve lançar exceção ao tentar aprovar pagamento em estado terminal")
        void deveBloquearAprovacaoEmEstadoTerminal() {
            Pagamento pagamento = criarCobrancaValida();
            pagamento.aprovar(MetodoPagamento.CARTAO_CREDITO, "AUTH-1", agora.plusSeconds(30));

            assertThatThrownBy(() -> pagamento.aprovar(MetodoPagamento.CARTAO_CREDITO, "AUTH-2", agora.plusSeconds(60)))
                    .isInstanceOf(TransicaoEstadoPagamentoInvalidaException.class);
        }
    }

    @Nested
    @DisplayName("Transição: Recusa")
    class Recusa {

        @Test
        @DisplayName("Deve recusar pagamento por negativa do gateway dentro do prazo")
        void deveRecusarDentroDoPrazo() {
            Pagamento pagamento = criarCobrancaValida();
            Instant momentoRecusa = agora.plus(Duration.ofMinutes(2));

            pagamento.recusar(MetodoPagamento.CARTAO_CREDITO, "Saldo insuficiente", momentoRecusa);

            assertThat(pagamento.getStatus()).isEqualTo(StatusPagamento.RECUSADO);
            assertThat(pagamento.getMetodoPagamento()).isEqualTo(MetodoPagamento.CARTAO_CREDITO);
            assertThat(pagamento.getMotivoRecusa()).isEqualTo("Saldo insuficiente");
            assertThat(pagamento.getAtualizadoEm()).isEqualTo(momentoRecusa);
        }

        @Test
        @DisplayName("Deve marcar como EXPIRADO se tentar recusar após timeout de 10 minutos")
        void deveExpirarSeRecusarAposPrazo() {
            Pagamento pagamento = criarCobrancaValida();
            Instant momentoAposPrazo = agora.plus(Duration.ofMinutes(11));

            pagamento.recusar(MetodoPagamento.CARTAO_CREDITO, "Cartao bloqueado", momentoAposPrazo);

            assertThat(pagamento.getStatus()).isEqualTo(StatusPagamento.EXPIRADO);
            assertThat(pagamento.getMotivoRecusa()).contains("expirou");
        }

        @Test
        @DisplayName("Deve lançar exceção ao tentar recusar pagamento em estado terminal")
        void deveBloquearRecusaEmEstadoTerminal() {
            Pagamento pagamento = criarCobrancaValida();
            pagamento.recusar(MetodoPagamento.CARTAO_CREDITO, "Cartao bloqueado", agora.plusSeconds(30));

            assertThatThrownBy(() -> pagamento.recusar(MetodoPagamento.CARTAO_CREDITO, "Outro motivo", agora.plusSeconds(60)))
                    .isInstanceOf(TransicaoEstadoPagamentoInvalidaException.class);
        }
    }

    @Nested
    @DisplayName("Transição: Expiração")
    class Expiracao {

        @Test
        @DisplayName("Deve marcar como EXPIRADO quando atingir ou passar da data limite")
        void deveExpirarAposLimite() {
            Pagamento pagamento = criarCobrancaValida();
            Instant instanteTimeout = agora.plus(Duration.ofMinutes(10));

            pagamento.expirar(instanteTimeout);

            assertThat(pagamento.getStatus()).isEqualTo(StatusPagamento.EXPIRADO);
            assertThat(pagamento.getMotivoRecusa()).contains("timeout de 10 minutos");
            assertThat(pagamento.getAtualizadoEm()).isEqualTo(instanteTimeout);
        }

        @Test
        @DisplayName("Deve ser idempotente se chamado novamente após já estar EXPIRADO")
        void deveSerIdempotenteSeJaExpirado() {
            Pagamento pagamento = criarCobrancaValida();
            Instant instanteTimeout = agora.plus(Duration.ofMinutes(10));

            pagamento.expirar(instanteTimeout);
            pagamento.expirar(instanteTimeout.plusSeconds(60)); // não deve lançar exceção

            assertThat(pagamento.getStatus()).isEqualTo(StatusPagamento.EXPIRADO);
        }

        @Test
        @DisplayName("Deve lançar PagamentoDomainException se tentar expirar antes do prazo")
        void deveRejeitarExpiracaoPrecoce() {
            Pagamento pagamento = criarCobrancaValida();
            Instant antesDoPrazo = agora.plus(Duration.ofMinutes(9));

            assertThatThrownBy(() -> pagamento.expirar(antesDoPrazo))
                    .isInstanceOf(PagamentoDomainException.class)
                    .hasMessageContaining("Nao e permitido expirar o pagamento");
        }
    }
}
