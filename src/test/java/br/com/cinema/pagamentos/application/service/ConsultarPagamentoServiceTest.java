package br.com.cinema.pagamentos.application.service;

import br.com.cinema.pagamentos.application.port.in.PagamentoResult;
import br.com.cinema.pagamentos.application.port.out.BuscarPagamentoPort;
import br.com.cinema.pagamentos.domain.exception.PagamentoNaoEncontradoException;
import br.com.cinema.pagamentos.domain.model.Pagamento;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários de Serviço - ConsultarPagamentoService")
class ConsultarPagamentoServiceTest {

    @Mock
    private BuscarPagamentoPort buscarPagamentoPort;

    @InjectMocks
    private ConsultarPagamentoService service;

    @Test
    @DisplayName("Deve retornar PagamentoResult ao buscar por ID existente")
    void deveBuscarPorId() {
        Pagamento pagamento = Pagamento.criarAguardandoPagamento(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("40.00"), Instant.now()
        );
        when(buscarPagamentoPort.buscarPorId(pagamento.getId())).thenReturn(Optional.of(pagamento));

        PagamentoResult result = service.buscarPorId(pagamento.getId());

        assertThat(result.id()).isEqualTo(pagamento.getId());
        assertThat(result.reservaId()).isEqualTo(pagamento.getReservaId());
    }

    @Test
    @DisplayName("Deve lançar PagamentoNaoEncontradoException ao buscar ID inexistente")
    void deveLancarQuandoIdNaoExiste() {
        UUID id = UUID.randomUUID();
        when(buscarPagamentoPort.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(id))
                .isInstanceOf(PagamentoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Deve retornar PagamentoResult ao buscar por reservaId existente")
    void deveBuscarPorReservaId() {
        Pagamento pagamento = Pagamento.criarAguardandoPagamento(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("40.00"), Instant.now()
        );
        when(buscarPagamentoPort.buscarPorReservaId(pagamento.getReservaId())).thenReturn(Optional.of(pagamento));

        PagamentoResult result = service.buscarPorReservaId(pagamento.getReservaId());

        assertThat(result.reservaId()).isEqualTo(pagamento.getReservaId());
    }

    @Test
    @DisplayName("Deve lançar PagamentoNaoEncontradoException ao buscar reservaId sem pagamento")
    void deveLancarQuandoReservaNaoExiste() {
        UUID reservaId = UUID.randomUUID();
        when(buscarPagamentoPort.buscarPorReservaId(reservaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorReservaId(reservaId))
                .isInstanceOf(PagamentoNaoEncontradoException.class);
    }
}
