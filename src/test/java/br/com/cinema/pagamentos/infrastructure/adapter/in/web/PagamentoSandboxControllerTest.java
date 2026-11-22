package br.com.cinema.pagamentos.infrastructure.adapter.in.web;

import br.com.cinema.pagamentos.application.port.in.ConsultarPagamentoUseCase;
import br.com.cinema.pagamentos.application.port.in.CriarCobrancaCommand;
import br.com.cinema.pagamentos.application.port.in.CriarCobrancaReservaUseCase;
import br.com.cinema.pagamentos.application.port.in.ExpirarPagamentosPendentesUseCase;
import br.com.cinema.pagamentos.application.port.in.PagamentoResult;
import br.com.cinema.pagamentos.domain.model.StatusPagamento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = PagamentoSandboxController.class)
@DisplayName("Testes da Camada de Templates - PagamentoSandboxController")
class PagamentoSandboxControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CriarCobrancaReservaUseCase criarCobrancaReservaUseCase;
    @MockitoBean
    private ConsultarPagamentoUseCase consultarPagamentoUseCase;
    @MockitoBean
    private ExpirarPagamentosPendentesUseCase expirarPagamentosPendentesUseCase;

    @Test
    @DisplayName("Deve renderizar a página do Sandbox de Pagamentos")
    void deveRenderizarSandbox() throws Exception {
        mockMvc.perform(get("/pagamentos/sandbox"))
                .andExpect(status().isOk())
                .andExpect(view().name("pagamentos/sandbox"));
    }

    @Test
    @DisplayName("Deve criar cobrança simulada e redirecionar para a tela de checkout")
    void deveCriarCobrancaERedirecionar() throws Exception {
        UUID pagamentoId = UUID.randomUUID();
        Instant agora = Instant.now();
        PagamentoResult criada = new PagamentoResult(
                pagamentoId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("45.00"), "RESERVA-SANDBOX", StatusPagamento.AGUARDANDO_PAGAMENTO,
                null, null, null, agora, agora.plus(Duration.ofMinutes(10)), agora
        );
        when(criarCobrancaReservaUseCase.executar(any(CriarCobrancaCommand.class))).thenReturn(criada);

        mockMvc.perform(post("/pagamentos/sandbox/criar")
                        .param("valor", "45.00")
                        .param("filme", "Duna")
                        .param("sala", "IMAX")
                        .param("horario", "21:00")
                        .param("assento", "G12"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/pagamentos/" + pagamentoId + "/checkout*"));
    }

    @Test
    @DisplayName("Deve simular expiração da cobrança acionando ExpirarPagamentosPendentesUseCase")
    void deveSimularExpiracao() throws Exception {
        UUID pagamentoId = UUID.randomUUID();
        Instant agora = Instant.now();
        Instant expiraEm = agora.plus(Duration.ofMinutes(10));
        PagamentoResult pendente = new PagamentoResult(
                pagamentoId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("45.00"), "RESERVA-SANDBOX", StatusPagamento.AGUARDANDO_PAGAMENTO,
                null, null, null, agora, expiraEm, agora
        );
        PagamentoResult expirado = new PagamentoResult(
                pagamentoId, pendente.reservaId(), pendente.sessaoId(), pendente.assentoId(), pendente.clienteId(),
                new BigDecimal("45.00"), "RESERVA-SANDBOX", StatusPagamento.EXPIRADO,
                null, null, "Expirado automaticamente por timeout de 10 minutos.", agora, expiraEm, expiraEm.plusSeconds(1)
        );

        when(consultarPagamentoUseCase.buscarPorId(pagamentoId)).thenReturn(pendente, expirado);

        mockMvc.perform(post("/pagamentos/sandbox/{id}/simular-expiracao", pagamentoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EXPIRADO"));

        verify(expirarPagamentosPendentesUseCase).expirarPagamentosVencidos(expiraEm.plusSeconds(1));
    }
}
