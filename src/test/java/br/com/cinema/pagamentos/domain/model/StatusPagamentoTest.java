package br.com.cinema.pagamentos.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes Unitários do Domínio - StatusPagamento")
class StatusPagamentoTest {

    @Test
    @DisplayName("AGUARDANDO_PAGAMENTO não deve ser estado terminal")
    void aguardandoPagamentoNaoETerminal() {
        assertThat(StatusPagamento.AGUARDANDO_PAGAMENTO.isTerminal()).isFalse();
    }

    @Test
    @DisplayName("APROVADO, RECUSADO e EXPIRADO devem ser estados terminais")
    void estadosTerminaisDevemSerVerdadeiros() {
        assertThat(StatusPagamento.APROVADO.isTerminal()).isTrue();
        assertThat(StatusPagamento.RECUSADO.isTerminal()).isTrue();
        assertThat(StatusPagamento.EXPIRADO.isTerminal()).isTrue();
    }
}
