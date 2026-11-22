package br.com.cinema.pagamentos.infrastructure.adapter.in.web;

import br.com.cinema.pagamentos.application.port.in.ConsultarPagamentoUseCase;
import br.com.cinema.pagamentos.application.port.in.PagamentoResult;
import br.com.cinema.pagamentos.domain.exception.PagamentoNaoEncontradoException;
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

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = PagamentoViewController.class)
@DisplayName("Testes da Camada de Templates - PagamentoViewController")
class PagamentoViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConsultarPagamentoUseCase consultarPagamentoUseCase;

    @Test
    @DisplayName("Deve renderizar a página de checkout para pagamento existente")
    void deveRenderizarCheckout() throws Exception {
        UUID pagamentoId = UUID.randomUUID();
        Instant agora = Instant.now();
        PagamentoResult resultado = new PagamentoResult(
                pagamentoId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("58.00"), "RESERVA-TEST", StatusPagamento.AGUARDANDO_PAGAMENTO,
                null, null, null, agora, agora.plus(Duration.ofMinutes(10)), agora
        );
        when(consultarPagamentoUseCase.buscarPorId(pagamentoId)).thenReturn(resultado);

        mockMvc.perform(get("/pagamentos/{id}/checkout", pagamentoId)
                        .param("filme", "Duna: Parte 2")
                        .param("assento", "H10"))
                .andExpect(status().isOk())
                .andExpect(view().name("pagamentos/checkout"))
                .andExpect(model().attributeExists("pagamento", "codigoPixSimulado", "sandboxAtivo"))
                .andExpect(content().string(containsString("Duna: Parte 2")))
                .andExpect(content().string(containsString("H10")));
    }

    @Test
    @DisplayName("Deve retornar 404 com página amigável quando pagamento não existir")
    void deveRenderizarNaoEncontradoQuandoInexistente() throws Exception {
        UUID pagamentoId = UUID.randomUUID();
        when(consultarPagamentoUseCase.buscarPorId(pagamentoId))
                .thenThrow(new PagamentoNaoEncontradoException(pagamentoId));

        mockMvc.perform(get("/pagamentos/{id}/checkout", pagamentoId))
                .andExpect(status().isNotFound())
                .andExpect(view().name("pagamentos/nao-encontrado"))
                .andExpect(content().string(containsString(pagamentoId.toString())));
    }
}
