package br.com.cinema.pagamentos;

import br.com.cinema.pagamentos.application.port.in.ConsultarPagamentoUseCase;
import br.com.cinema.pagamentos.application.port.in.ExpirarPagamentosPendentesUseCase;
import br.com.cinema.pagamentos.application.port.in.PagamentoResult;
import br.com.cinema.pagamentos.application.port.in.ProcessarPagamentoCommand;
import br.com.cinema.pagamentos.application.port.in.ProcessarPagamentoUseCase;
import br.com.cinema.pagamentos.domain.model.MetodoPagamento;
import br.com.cinema.pagamentos.domain.model.StatusPagamento;
import br.com.cinema.pagamentos.infrastructure.adapter.in.messaging.AssentoReservadoEventListener;
import br.com.cinema.shared.events.AssentoReservadoEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Testes de Integração - Fluxo Completo de Pagamento")
class FluxoPagamentoIntegrationTest {

    @Autowired
    private AssentoReservadoEventListener eventListener;

    @Autowired
    private ConsultarPagamentoUseCase consultarPagamentoUseCase;

    @Autowired
    private ProcessarPagamentoUseCase processarPagamentoUseCase;

    @Autowired
    private ExpirarPagamentosPendentesUseCase expirarPagamentosPendentesUseCase;

    @Test
    @DisplayName("Deve criar cobranca de forma idempotente ao receber AssentoReservadoEvent duplicado e aprovar no cartao")
    void deveCriarCobrancaIdempotenteEAprovarPagamento() {
        UUID reservaId = UUID.randomUUID();
        AssentoReservadoEvent eventoFalso = new AssentoReservadoEvent(
                reservaId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("45.00"),
                Instant.now()
        );

        // 1. Dispara o mesmo evento 2 vezes para provar idempotência
        eventListener.onAssentoReservado(eventoFalso);
        eventListener.onAssentoReservado(eventoFalso);

        PagamentoResult cobranca = consultarPagamentoUseCase.buscarPorReservaId(reservaId);
        assertThat(cobranca.status()).isEqualTo(StatusPagamento.AGUARDANDO_PAGAMENTO);

        // 2. Processa pagamento com cartão final 4242 (aprovado no FakePaymentGatewayAdapter)
        PagamentoResult aprovado = processarPagamentoUseCase.executar(new ProcessarPagamentoCommand(
                cobranca.id(),
                MetodoPagamento.CARTAO_CREDITO,
                "4111 1111 1111 4242",
                "JOAO V NOVAES",
                "12/2030",
                "123"
        ));

        assertThat(aprovado.status()).isEqualTo(StatusPagamento.APROVADO);
        assertThat(aprovado.codigoTransacaoGateway()).startsWith("AUTH-");
    }

    @Test
    @DisplayName("Deve expirar cobranca pendente apos timeout de 10 minutos")
    void deveExpirarCobrancaAposDezMinutos() {
        UUID reservaId = UUID.randomUUID();
        eventListener.onAssentoReservado(new AssentoReservadoEvent(
                reservaId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("30.00"),
                Instant.now()
        ));

        // Simula avanço do relógio em 10 minutos e 1 segundo
        Instant futuro = Instant.now().plus(Duration.ofMinutes(10).plusSeconds(1));
        int totalExpirados = expirarPagamentosPendentesUseCase.expirarPagamentosVencidos(futuro);

        assertThat(totalExpirados).isGreaterThanOrEqualTo(1);
        PagamentoResult expirado = consultarPagamentoUseCase.buscarPorReservaId(reservaId);
        assertThat(expirado.status()).isEqualTo(StatusPagamento.EXPIRADO);
    }
}
