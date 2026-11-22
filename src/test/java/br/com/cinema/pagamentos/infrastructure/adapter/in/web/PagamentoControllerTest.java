package br.com.cinema.pagamentos.infrastructure.adapter.in.web;

import br.com.cinema.pagamentos.application.port.in.ConsultarPagamentoUseCase;
import br.com.cinema.pagamentos.application.port.in.PagamentoResult;
import br.com.cinema.pagamentos.application.port.in.ProcessarPagamentoCommand;
import br.com.cinema.pagamentos.application.port.in.ProcessarPagamentoUseCase;
import br.com.cinema.pagamentos.domain.exception.PagamentoExpiradoException;
import br.com.cinema.pagamentos.domain.exception.PagamentoNaoEncontradoException;
import br.com.cinema.pagamentos.domain.exception.TransicaoEstadoPagamentoInvalidaException;
import br.com.cinema.pagamentos.domain.model.MetodoPagamento;
import br.com.cinema.pagamentos.domain.model.StatusPagamento;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PagamentoController.class)
@DisplayName("Testes do Driving Adapter Web - PagamentoController")
class PagamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private ProcessarPagamentoUseCase processarPagamentoUseCase;

    @MockitoBean
    private ConsultarPagamentoUseCase consultarPagamentoUseCase;

    private final UUID pagamentoId = UUID.randomUUID();
    private final UUID reservaId = UUID.randomUUID();
    private final UUID sessaoId = UUID.randomUUID();
    private final UUID assentoId = UUID.randomUUID();
    private final UUID clienteId = UUID.randomUUID();
    private final Instant agora = Instant.now();

    private PagamentoResult criarResultado(StatusPagamento status, String codigoAuth, String motivoRecusa) {
        return new PagamentoResult(
                pagamentoId,
                reservaId,
                sessaoId,
                assentoId,
                clienteId,
                new BigDecimal("45.00"),
                "RESERVA-" + reservaId,
                status,
                MetodoPagamento.CARTAO_CREDITO,
                codigoAuth,
                motivoRecusa,
                agora,
                agora.plus(Duration.ofMinutes(10)),
                agora
        );
    }

    @Nested
    @DisplayName("POST /api/v1/pagamentos/{id}/processar")
    class ProcessarPagamentoEndpoint {

        @Test
        @DisplayName("Deve processar pagamento com sucesso e retornar 200 OK")
        void deveProcessarPagamentoComSucesso() throws Exception {
            PagamentoResult resultado = criarResultado(StatusPagamento.APROVADO, "AUTH-123456", null);
            when(processarPagamentoUseCase.executar(any(ProcessarPagamentoCommand.class))).thenReturn(resultado);

            ProcessarPagamentoRequest request = new ProcessarPagamentoRequest(
                    MetodoPagamento.CARTAO_CREDITO,
                    "4111 1111 1111 4242",
                    "JOAO V NOVAES",
                    "12/2030",
                    "123"
            );

            mockMvc.perform(post("/api/v1/pagamentos/{id}/processar", pagamentoId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(pagamentoId.toString()))
                    .andExpect(jsonPath("$.status").value("APROVADO"))
                    .andExpect(jsonPath("$.codigoTransacaoGateway").value("AUTH-123456"))
                    .andExpect(jsonPath("$.valor").value(45.00));
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request quando os dados do cartão forem inválidos")
        void deveRejeitarRequisicaoComDadosInvalidos() throws Exception {
            ProcessarPagamentoRequest requestInvalido = new ProcessarPagamentoRequest(
                    null, // Metodo nulo
                    "123", // Cartao curto demais
                    "", // Nome em branco
                    "99/99", // Mes invalido
                    "cvv" // CVV nao numerico
            );

            mockMvc.perform(post("/api/v1/pagamentos/{id}/processar", pagamentoId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestInvalido)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Erro de Validação de Dados"))
                    .andExpect(jsonPath("$.erros.metodoPagamento").exists())
                    .andExpect(jsonPath("$.erros.numeroCartao").exists())
                    .andExpect(jsonPath("$.erros.nomeTitular").exists())
                    .andExpect(jsonPath("$.erros.validade").exists())
                    .andExpect(jsonPath("$.erros.cvv").exists());
        }

        @Test
        @DisplayName("Deve retornar 422 Unprocessable Entity quando o pagamento estiver expirado")
        void deveRetornar422QuandoExpirado() throws Exception {
            when(processarPagamentoUseCase.executar(any(ProcessarPagamentoCommand.class)))
                    .thenThrow(new PagamentoExpiradoException(pagamentoId, agora));

            ProcessarPagamentoRequest request = new ProcessarPagamentoRequest(
                    MetodoPagamento.CARTAO_CREDITO,
                    "4111 1111 1111 4242",
                    "JOAO V NOVAES",
                    "12/2030",
                    "123"
            );

            mockMvc.perform(post("/api/v1/pagamentos/{id}/processar", pagamentoId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.title").value("Pagamento Expirado"))
                    .andExpect(jsonPath("$.motivo").exists());
        }

        @Test
        @DisplayName("Deve retornar 409 Conflict quando tentar transicionar estado terminal")
        void deveRetornar409QuandoTransicaoInvalida() throws Exception {
            when(processarPagamentoUseCase.executar(any(ProcessarPagamentoCommand.class)))
                    .thenThrow(new TransicaoEstadoPagamentoInvalidaException("APROVADO", "APROVAR"));

            ProcessarPagamentoRequest request = new ProcessarPagamentoRequest(
                    MetodoPagamento.CARTAO_CREDITO,
                    "4111 1111 1111 4242",
                    "JOAO V NOVAES",
                    "12/2030",
                    "123"
            );

            mockMvc.perform(post("/api/v1/pagamentos/{id}/processar", pagamentoId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Transição de Estado Inválida"));
        }

        @Test
        @DisplayName("Deve processar pagamento via PIX sem exigir dados de cartão e retornar 200 OK")
        void deveProcessarPagamentoPixSemDadosCartao() throws Exception {
            PagamentoResult resultadoPix = new PagamentoResult(
                    pagamentoId, reservaId, sessaoId, assentoId, clienteId,
                    new BigDecimal("45.00"), "RESERVA-" + reservaId,
                    StatusPagamento.APROVADO, MetodoPagamento.PIX, "PIX-ABC12345", null,
                    agora, agora.plus(Duration.ofMinutes(10)), agora
            );
            when(processarPagamentoUseCase.executar(any(ProcessarPagamentoCommand.class))).thenReturn(resultadoPix);

            ProcessarPagamentoRequest requestPix = new ProcessarPagamentoRequest(
                    MetodoPagamento.PIX, null, null, null, null
            );

            mockMvc.perform(post("/api/v1/pagamentos/{id}/processar", pagamentoId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestPix)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("APROVADO"))
                    .andExpect(jsonPath("$.metodoPagamento").value("PIX"))
                    .andExpect(jsonPath("$.codigoTransacaoGateway").value("PIX-ABC12345"));
        }

        @Test
        @DisplayName("Deve retornar 409 Conflict quando ocorrer conflito de concorrência (@Version)")
        void deveRetornar409QuandoHouverConflitoOtimista() throws Exception {
            when(processarPagamentoUseCase.executar(any(ProcessarPagamentoCommand.class)))
                    .thenThrow(new ObjectOptimisticLockingFailureException("PagamentoJpaEntity", pagamentoId));

            ProcessarPagamentoRequest request = new ProcessarPagamentoRequest(
                    MetodoPagamento.PIX, null, null, null, null
            );

            mockMvc.perform(post("/api/v1/pagamentos/{id}/processar", pagamentoId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Conflito de Concorrência no Pagamento"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/pagamentos/{id}")
    class ConsultarPorIdEndpoint {

        @Test
        @DisplayName("Deve retornar 200 OK com os dados do pagamento existente")
        void deveRetornarPagamentoExistente() throws Exception {
            PagamentoResult resultado = criarResultado(StatusPagamento.AGUARDANDO_PAGAMENTO, null, null);
            when(consultarPagamentoUseCase.buscarPorId(pagamentoId)).thenReturn(resultado);

            mockMvc.perform(get("/api/v1/pagamentos/{id}", pagamentoId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(pagamentoId.toString()))
                    .andExpect(jsonPath("$.status").value("AGUARDANDO_PAGAMENTO"))
                    .andExpect(jsonPath("$.segundosRestantesParaExpirar").isNumber());
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found quando pagamento não existir")
        void deveRetornar404QuandoNaoEncontrado() throws Exception {
            when(consultarPagamentoUseCase.buscarPorId(pagamentoId))
                    .thenThrow(new PagamentoNaoEncontradoException(pagamentoId));

            mockMvc.perform(get("/api/v1/pagamentos/{id}", pagamentoId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Pagamento Não Encontrado"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/pagamentos/reserva/{reservaId}")
    class ConsultarPorReservaEndpoint {

        @Test
        @DisplayName("Deve retornar 200 OK com pagamento vinculado à reserva")
        void deveRetornarPagamentoPorReserva() throws Exception {
            PagamentoResult resultado = criarResultado(StatusPagamento.APROVADO, "AUTH-999", null);
            when(consultarPagamentoUseCase.buscarPorReservaId(reservaId)).thenReturn(resultado);

            mockMvc.perform(get("/api/v1/pagamentos/reserva/{reservaId}", reservaId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.reservaId").value(reservaId.toString()))
                    .andExpect(jsonPath("$.status").value("APROVADO"));
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found quando a reserva não tiver pagamento")
        void deveRetornar404QuandoReservaNaoTiverPagamento() throws Exception {
            when(consultarPagamentoUseCase.buscarPorReservaId(reservaId))
                    .thenThrow(new PagamentoNaoEncontradoException("Pagamento nao encontrado para a reserva [" + reservaId + "]."));

            mockMvc.perform(get("/api/v1/pagamentos/reserva/{reservaId}", reservaId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Pagamento Não Encontrado"));
        }
    }
}
