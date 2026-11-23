package br.com.cinema.pagamentos.infrastructure.adapter.out.gateway;

import br.com.cinema.pagamentos.application.port.out.RespostaGateway;
import br.com.cinema.pagamentos.domain.model.DadosCartao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes Unitários do Gateway Fake - FakePaymentGatewayAdapter")
class FakePaymentGatewayAdapterTest {

    private final FakePaymentGatewayAdapter adapter = new FakePaymentGatewayAdapter();

    @Test
    @DisplayName("Deve aprovar cartão com final 4242 gerando código AUTH-")
    void deveAprovarCartaoValido() {
        RespostaGateway resp = adapter.processarCobranca(
                UUID.randomUUID(),
                new BigDecimal("45.00"),
                new DadosCartao("4111 1111 1111 4242", "TESTE", "12/2030", "123")
        );

        assertThat(resp.aprovado()).isTrue();
        assertThat(resp.codigoTransacao()).startsWith("AUTH-");
        assertThat(resp.motivoRecusa()).isNull();
    }

    @Test
    @DisplayName("Deve recusar cartão com final 0000")
    void deveRecusarCartaoFinalZero() {
        RespostaGateway resp = adapter.processarCobranca(
                UUID.randomUUID(),
                new BigDecimal("45.00"),
                new DadosCartao("5500 0000 0000 0000", "TESTE", "12/2030", "123")
        );

        assertThat(resp.aprovado()).isFalse();
        assertThat(resp.codigoTransacao()).isNull();
        assertThat(resp.motivoRecusa()).contains("negada");
    }

    @Test
    @DisplayName("Deve aprovar PIX gerando código PIX-")
    void deveAprovarPix() {
        RespostaGateway resp = adapter.processarPix(UUID.randomUUID(), new BigDecimal("45.00"));

        assertThat(resp.aprovado()).isTrue();
        assertThat(resp.codigoTransacao()).startsWith("PIX-");
        assertThat(resp.motivoRecusa()).isNull();
    }
}
