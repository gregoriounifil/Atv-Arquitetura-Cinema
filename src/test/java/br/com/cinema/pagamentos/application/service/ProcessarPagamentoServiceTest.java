package br.com.cinema.pagamentos.application.service;

import br.com.cinema.pagamentos.application.port.in.PagamentoResult;
import br.com.cinema.pagamentos.application.port.in.ProcessarPagamentoCommand;
import br.com.cinema.pagamentos.application.port.out.BuscarPagamentoPort;
import br.com.cinema.pagamentos.application.port.out.GatewayCartaoPort;
import br.com.cinema.pagamentos.application.port.out.PublicarEventoPagamentoPort;
import br.com.cinema.pagamentos.application.port.out.RespostaGateway;
import br.com.cinema.pagamentos.application.port.out.SalvarPagamentoPort;
import br.com.cinema.pagamentos.domain.exception.PagamentoExpiradoException;
import br.com.cinema.pagamentos.domain.exception.PagamentoNaoEncontradoException;
import br.com.cinema.pagamentos.domain.exception.TransicaoEstadoPagamentoInvalidaException;
import br.com.cinema.pagamentos.domain.model.DadosCartao;
import br.com.cinema.pagamentos.domain.model.MetodoPagamento;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários de Serviço - ProcessarPagamentoService")
class ProcessarPagamentoServiceTest {

    @Mock
    private BuscarPagamentoPort buscarPagamentoPort;
    @Mock
    private SalvarPagamentoPort salvarPagamentoPort;
    @Mock
    private GatewayCartaoPort gatewayCartaoPort;
    @Mock
    private PublicarEventoPagamentoPort publicarEventoPagamentoPort;

    @InjectMocks
    private ProcessarPagamentoService service;

    private Pagamento criarPagamentoPendente(Instant criadoEm) {
        return Pagamento.criarAguardandoPagamento(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("45.00"),
                criadoEm
        );
    }

    @Test
    @DisplayName("Deve aprovar pagamento com cartão e publicar PagamentoAprovadoEvent")
    void deveAprovarPagamentoCartao() {
        Pagamento pendente = criarPagamentoPendente(Instant.now());
        when(buscarPagamentoPort.buscarPorId(pendente.getId())).thenReturn(Optional.of(pendente));
        when(gatewayCartaoPort.processarCobranca(eq(pendente.getId()), eq(pendente.getValor()), any(DadosCartao.class)))
                .thenReturn(new RespostaGateway(true, "AUTH-998877", null));
        when(salvarPagamentoPort.salvar(any(Pagamento.class))).thenAnswer(inv -> inv.getArgument(0));

        PagamentoResult result = service.executar(new ProcessarPagamentoCommand(
                pendente.getId(),
                MetodoPagamento.CARTAO_CREDITO,
                "4111 1111 1111 4242",
                "JOAO V NOVAES",
                "12/2030",
                "123"
        ));

        assertThat(result.status()).isEqualTo(StatusPagamento.APROVADO);
        assertThat(result.codigoTransacaoGateway()).isEqualTo("AUTH-998877");
        verify(publicarEventoPagamentoPort).publicarPagamentoAprovado(any());
        verify(publicarEventoPagamentoPort, never()).publicarPagamentoRecusado(any());
    }

    @Test
    @DisplayName("Deve recusar pagamento quando gateway negar e publicar PagamentoRecusadoEvent")
    void deveRecusarPagamentoCartao() {
        Pagamento pendente = criarPagamentoPendente(Instant.now());
        when(buscarPagamentoPort.buscarPorId(pendente.getId())).thenReturn(Optional.of(pendente));
        when(gatewayCartaoPort.processarCobranca(eq(pendente.getId()), eq(pendente.getValor()), any(DadosCartao.class)))
                .thenReturn(new RespostaGateway(false, null, "Limite insuficiente"));
        when(salvarPagamentoPort.salvar(any(Pagamento.class))).thenAnswer(inv -> inv.getArgument(0));

        PagamentoResult result = service.executar(new ProcessarPagamentoCommand(
                pendente.getId(),
                MetodoPagamento.CARTAO_DEBITO,
                "5500 0000 0000 0000",
                "JOAO V NOVAES",
                "12/2030",
                "123"
        ));

        assertThat(result.status()).isEqualTo(StatusPagamento.RECUSADO);
        assertThat(result.motivoRecusa()).isEqualTo("Limite insuficiente");
        verify(publicarEventoPagamentoPort).publicarPagamentoRecusado(any());
        verify(publicarEventoPagamentoPort, never()).publicarPagamentoAprovado(any());
    }

    @Test
    @DisplayName("Deve processar pagamento via PIX sem exigir dados de cartão")
    void deveProcessarPagamentoPix() {
        Pagamento pendente = criarPagamentoPendente(Instant.now());
        when(buscarPagamentoPort.buscarPorId(pendente.getId())).thenReturn(Optional.of(pendente));
        when(gatewayCartaoPort.processarPix(pendente.getId(), pendente.getValor()))
                .thenReturn(new RespostaGateway(true, "PIX-12345678", null));
        when(salvarPagamentoPort.salvar(any(Pagamento.class))).thenAnswer(inv -> inv.getArgument(0));

        PagamentoResult result = service.executar(new ProcessarPagamentoCommand(
                pendente.getId(),
                MetodoPagamento.PIX,
                null,
                null,
                null,
                null
        ));

        assertThat(result.status()).isEqualTo(StatusPagamento.APROVADO);
        assertThat(result.metodoPagamento()).isEqualTo(MetodoPagamento.PIX);
        assertThat(result.codigoTransacaoGateway()).isEqualTo("PIX-12345678");
        verify(gatewayCartaoPort, never()).processarCobranca(any(), any(), any());
        verify(publicarEventoPagamentoPort).publicarPagamentoAprovado(any());
    }

    @Test
    @DisplayName("Deve aplicar lazy expiration antes de chamar o gateway quando cobrança já estiver vencida")
    void deveExpirarAntesDoGatewaySeVencido() {
        Pagamento vencido = criarPagamentoPendente(Instant.now().minus(Duration.ofMinutes(11)));
        when(buscarPagamentoPort.buscarPorId(vencido.getId())).thenReturn(Optional.of(vencido));

        assertThatThrownBy(() -> service.executar(new ProcessarPagamentoCommand(
                vencido.getId(),
                MetodoPagamento.PIX,
                null, null, null, null
        ))).isInstanceOf(PagamentoExpiradoException.class);

        verifyNoInteractions(gatewayCartaoPort);
        verify(salvarPagamentoPort).salvar(vencido);
        verify(publicarEventoPagamentoPort).publicarPagamentoExpirado(any());
    }

    @Test
    @DisplayName("Deve persistir EXPIRADO e publicar PagamentoExpiradoEvent se vencer durante chamada ao gateway")
    void deveExpirarEPublicarSeVencerDuranteChamadaAoGateway() {
        // Falta 40ms para expirar; gateway simula 120ms de rede
        Instant criadoEm = Instant.now().minus(Duration.ofMinutes(10)).plusMillis(40);
        Pagamento quaseVencido = criarPagamentoPendente(criadoEm);
        when(buscarPagamentoPort.buscarPorId(quaseVencido.getId())).thenReturn(Optional.of(quaseVencido));
        when(gatewayCartaoPort.processarPix(quaseVencido.getId(), quaseVencido.getValor()))
                .thenAnswer(inv -> {
                    Thread.sleep(120);
                    return new RespostaGateway(true, "PIX-TARDE", null);
                });

        assertThatThrownBy(() -> service.executar(new ProcessarPagamentoCommand(
                quaseVencido.getId(),
                MetodoPagamento.PIX,
                null, null, null, null
        ))).isInstanceOf(PagamentoExpiradoException.class);

        assertThat(quaseVencido.getStatus()).isEqualTo(StatusPagamento.EXPIRADO);
        verify(salvarPagamentoPort).salvar(quaseVencido);
        verify(publicarEventoPagamentoPort).publicarPagamentoExpirado(any());
        verify(publicarEventoPagamentoPort, never()).publicarPagamentoAprovado(any());
    }

    @Test
    @DisplayName("Deve lançar PagamentoNaoEncontradoException quando ID não existir")
    void deveLancarQuandoNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(buscarPagamentoPort.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.executar(new ProcessarPagamentoCommand(
                id, MetodoPagamento.PIX, null, null, null, null
        ))).isInstanceOf(PagamentoNaoEncontradoException.class);

        verifyNoInteractions(gatewayCartaoPort, salvarPagamentoPort, publicarEventoPagamentoPort);
    }

    @Test
    @DisplayName("Deve lançar TransicaoEstadoPagamentoInvalidaException quando pagamento já estiver em estado terminal")
    void deveLancarQuandoEstadoTerminal() {
        Pagamento aprovado = criarPagamentoPendente(Instant.now());
        aprovado.aprovar(MetodoPagamento.PIX, "PIX-OK", Instant.now());
        when(buscarPagamentoPort.buscarPorId(aprovado.getId())).thenReturn(Optional.of(aprovado));

        assertThatThrownBy(() -> service.executar(new ProcessarPagamentoCommand(
                aprovado.getId(), MetodoPagamento.PIX, null, null, null, null
        ))).isInstanceOf(TransicaoEstadoPagamentoInvalidaException.class);

        verifyNoInteractions(gatewayCartaoPort, salvarPagamentoPort, publicarEventoPagamentoPort);
    }
}
