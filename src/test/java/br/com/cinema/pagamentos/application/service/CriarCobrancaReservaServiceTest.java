package br.com.cinema.pagamentos.application.service;

import br.com.cinema.pagamentos.application.port.in.CriarCobrancaCommand;
import br.com.cinema.pagamentos.application.port.in.PagamentoResult;
import br.com.cinema.pagamentos.application.port.out.BuscarPagamentoPort;
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
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários de Serviço - CriarCobrancaReservaService")
class CriarCobrancaReservaServiceTest {

    @Mock
    private BuscarPagamentoPort buscarPagamentoPort;
    @Mock
    private SalvarPagamentoPort salvarPagamentoPort;

    @InjectMocks
    private CriarCobrancaReservaService service;

    @Test
    @DisplayName("Deve criar nova cobrança quando ainda não existir pagamento para a reserva")
    void deveCriarNovaCobranca() {
        CriarCobrancaCommand command = new CriarCobrancaCommand(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("50.00")
        );
        when(buscarPagamentoPort.buscarPorReservaId(command.reservaId())).thenReturn(Optional.empty());
        when(salvarPagamentoPort.salvar(any(Pagamento.class))).thenAnswer(inv -> inv.getArgument(0));

        PagamentoResult result = service.executar(command);

        assertThat(result.reservaId()).isEqualTo(command.reservaId());
        assertThat(result.status()).isEqualTo(StatusPagamento.AGUARDANDO_PAGAMENTO);
        assertThat(result.valor()).isEqualByComparingTo("50.00");
        verify(salvarPagamentoPort).salvar(any(Pagamento.class));
    }

    @Test
    @DisplayName("Deve retornar cobrança existente de forma idempotente quando reservaId já possuir pagamento")
    void deveSerIdempotenteQuandoReservaJaExistir() {
        UUID reservaId = UUID.randomUUID();
        Pagamento existente = Pagamento.criarAguardandoPagamento(
                reservaId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("50.00"), Instant.now()
        );
        when(buscarPagamentoPort.buscarPorReservaId(reservaId)).thenReturn(Optional.of(existente));

        PagamentoResult result = service.executar(new CriarCobrancaCommand(
                reservaId, existente.getSessaoId(), existente.getAssentoId(), existente.getClienteId(), new BigDecimal("50.00")
        ));

        assertThat(result.id()).isEqualTo(existente.getId());
        verify(salvarPagamentoPort, never()).salvar(any());
    }
}
