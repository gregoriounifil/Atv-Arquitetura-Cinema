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
import br.com.cinema.shared.events.PagamentoAprovadoEvent;
import br.com.cinema.shared.events.PagamentoExpiradoEvent;
import br.com.cinema.shared.events.PagamentoRecusadoEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@RecordApplicationEvents
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

    @Autowired
    private ApplicationEvents applicationEvents;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final List<UUID> pagamentosCriados = new ArrayList<>();

    @AfterEach
    void limparRegistrosDeTeste() {
        for (UUID id : pagamentosCriados) {
            jdbcTemplate.update("DELETE FROM pagamentos WHERE id = ?", id);
        }
        pagamentosCriados.clear();
    }

    private PagamentoResult dispararReservaEConsultar(UUID reservaId, BigDecimal valor) {
        AssentoReservadoEvent evento = new AssentoReservadoEvent(
                reservaId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                valor,
                Instant.now()
        );
        eventListener.onAssentoReservado(evento);
        PagamentoResult cobranca = consultarPagamentoUseCase.buscarPorReservaId(reservaId);
        pagamentosCriados.add(cobranca.id());
        return cobranca;
    }

    @Test
    @DisplayName("Deve criar cobranca de forma idempotente ao receber AssentoReservadoEvent duplicado, aprovar no cartao e publicar PagamentoAprovadoEvent")
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
        pagamentosCriados.add(cobranca.id());
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
        assertThat(applicationEvents.stream(PagamentoAprovadoEvent.class))
                .anyMatch(ev -> ev.pagamentoId().equals(cobranca.id()) && ev.reservaId().equals(reservaId));
    }

    @Test
    @DisplayName("Deve recusar pagamento com cartão final 0000 e publicar PagamentoRecusadoEvent")
    void deveRecusarCartaoFinalZeroEPublicarEvento() {
        UUID reservaId = UUID.randomUUID();
        PagamentoResult cobranca = dispararReservaEConsultar(reservaId, new BigDecimal("52.00"));

        PagamentoResult recusado = processarPagamentoUseCase.executar(new ProcessarPagamentoCommand(
                cobranca.id(),
                MetodoPagamento.CARTAO_CREDITO,
                "5500 0000 0000 0000",
                "JOAO V NOVAES",
                "12/2030",
                "123"
        ));

        assertThat(recusado.status()).isEqualTo(StatusPagamento.RECUSADO);
        assertThat(recusado.motivoRecusa()).contains("negada");
        assertThat(applicationEvents.stream(PagamentoRecusadoEvent.class))
                .anyMatch(ev -> ev.pagamentoId().equals(cobranca.id()) && ev.reservaId().equals(reservaId));
    }

    @Test
    @DisplayName("Deve aprovar pagamento via PIX ponta-a-ponta via endpoint REST")
    void deveProcessarPixViaEndpointHttpPontaAPonta() throws Exception {
        UUID reservaId = UUID.randomUUID();
        PagamentoResult cobranca = dispararReservaEConsultar(reservaId, new BigDecimal("38.50"));

        mockMvc.perform(post("/api/v1/pagamentos/{id}/processar", cobranca.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "metodoPagamento": "PIX"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADO"))
                .andExpect(jsonPath("$.metodoPagamento").value("PIX"));

        mockMvc.perform(get("/pagamentos/{id}/checkout", cobranca.id()))
                .andExpect(status().isOk());

        assertThat(applicationEvents.stream(PagamentoAprovadoEvent.class))
                .anyMatch(ev -> ev.pagamentoId().equals(cobranca.id()));
    }

    @Test
    @DisplayName("Deve expirar cobranca pendente apos timeout de 10 minutos e publicar PagamentoExpiradoEvent")
    void deveExpirarCobrancaAposDezMinutos() {
        UUID reservaId = UUID.randomUUID();
        PagamentoResult cobranca = dispararReservaEConsultar(reservaId, new BigDecimal("30.00"));

        // Simula avanço do relógio em 10 minutos e 1 segundo
        Instant futuro = Instant.now().plus(Duration.ofMinutes(10).plusSeconds(1));
        int totalExpirados = expirarPagamentosPendentesUseCase.expirarPagamentosVencidos(futuro);

        assertThat(totalExpirados).isGreaterThanOrEqualTo(1);
        PagamentoResult expirado = consultarPagamentoUseCase.buscarPorReservaId(reservaId);
        assertThat(expirado.status()).isEqualTo(StatusPagamento.EXPIRADO);
        assertThat(applicationEvents.stream(PagamentoExpiradoEvent.class))
                .anyMatch(ev -> ev.pagamentoId().equals(cobranca.id()) && ev.reservaId().equals(reservaId));
    }
}
