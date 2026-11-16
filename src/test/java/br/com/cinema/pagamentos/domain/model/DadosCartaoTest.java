package br.com.cinema.pagamentos.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes Unitários do Domínio - DadosCartao")
class DadosCartaoTest {

    @Test
    @DisplayName("Deve extrair os últimos 4 dígitos limpando espaços")
    void deveObterUltimosQuatroDigitosComEspacos() {
        DadosCartao dados = new DadosCartao("4111 2222 3333 4242", "JOAO V NOVAES", "12/2030", "123");
        assertThat(dados.obterUltimosQuatroDigitos()).isEqualTo("4242");
    }

    @Test
    @DisplayName("Deve extrair os últimos 4 dígitos sem espaços")
    void deveObterUltimosQuatroDigitosSemEspacos() {
        DadosCartao dados = new DadosCartao("4111222233339999", "JOAO V NOVAES", "12/2030", "123");
        assertThat(dados.obterUltimosQuatroDigitos()).isEqualTo("9999");
    }

    @Test
    @DisplayName("Deve retornar máscara quando número tiver menos de 4 dígitos")
    void deveRetornarMascaraParaNumeroCurto() {
        DadosCartao dados = new DadosCartao("12", "JOAO V NOVAES", "12/2030", "123");
        assertThat(dados.obterUltimosQuatroDigitos()).isEqualTo("****");
    }

    @Test
    @DisplayName("Deve lançar NullPointerException se qualquer campo for nulo")
    void deveRejeitarCamposNulos() {
        assertThatThrownBy(() -> new DadosCartao(null, "NOME", "12/30", "123"))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new DadosCartao("1234", null, "12/30", "123"))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new DadosCartao("1234", "NOME", null, "123"))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new DadosCartao("1234", "NOME", "12/30", null))
                .isInstanceOf(NullPointerException.class);
    }
}
