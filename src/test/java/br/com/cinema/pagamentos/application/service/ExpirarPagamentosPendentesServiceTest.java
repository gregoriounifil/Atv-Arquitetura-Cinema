package br.com.cinema.pagamentos.application.service;

import br.com.cinema.pagamentos.application.port.out.BuscarPagamentoPort;
import br.com.cinema.pagamentos.application.port.out.PublicarEventoPagamentoPort;
import br.com.cinema.pagamentos.application.port.out.SalvarPagamentoPort;
import br.com.cinema.pagamentos.domain.model.Pagamento;
import br.com.cinema.pagamentos.domain.model.StatusPagamento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários de Serviço - ExpirarPagamentosPendentesService")
class ExpirarPagamentosPendentesServiceTest {

    @Mock
    private BuscarPagamentoPort buscarPagamentoPort;
    @Mock
    private SalvarPagamentoPort salvarPagamentoPort;
    @Mock
    private PublicarEventoPagamentoPort publicarEventoPagamentoPort;

    @InjectMocks
    private ExpirarPagamentosPendentesService service;

    @Test
    @DisplayName("Deve expirar todos os pagamentos vencidos do lote e publicar eventos")
    void deveExpirarLoteEPublicarEventos() {
        Instant criacao = Instant.now().minus(Duration.ofMinutes(15));
        Pagamento p1 = Pagamento.criarAguardandoPagamento(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("35.00"), criacao
        );
        Pagamento p2 = Pagamento.criarAguardandoPagamento(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("45.00"), criacao
        );

        Instant agora = Instant.now();
        when(buscarPagamentoPort.buscarPendentesVencidos(agora, 100)).thenReturn(List.of(p1, p2));

        int total = service.expirarPagamentosVencidos(agora);

        assertThat(total).isEqualTo(2);
        assertThat(p1.getStatus()).isEqualTo(StatusPagamento.EXPIRADO);
        assertThat(p2.getStatus()).isEqualTo(StatusPagamento.EXPIRADO);
        verify(salvarPagamentoPort, times(2)).salvar(any(Pagamento.class));
        verify(publicarEventoPagamentoPort, times(2)).publicarPagamentoExpirado(any());
    }
}
