package br.com.cinema.pagamentos.infrastructure.adapter.out.persistence;

import br.com.cinema.pagamentos.domain.model.MetodoPagamento;
import br.com.cinema.pagamentos.domain.model.Pagamento;
import br.com.cinema.pagamentos.domain.model.StatusPagamento;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DisplayName("Testes de Integração da Persistência - PagamentoPersistenceAdapter")
class PagamentoPersistenceAdapterTest {

    @Autowired
    private PagamentoPersistenceAdapter persistenceAdapter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final List<UUID> pagamentosCriados = new ArrayList<>();

    @AfterEach
    void limparRegistrosDeTeste() {
        for (UUID id : pagamentosCriados) {
            jdbcTemplate.update("DELETE FROM pagamentos WHERE id = ?", id);
        }
        pagamentosCriados.clear();
    }

    @Test
    @DisplayName("Deve tratar violação de unicidade de reservaId retornando o registro existente")
    void deveRetornarExistenteQuandoHouverDuplicidadeDeReservaId() {
        UUID reservaId = UUID.randomUUID();
        Pagamento primeiro = Pagamento.criarAguardandoPagamento(
                reservaId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("45.00"), Instant.now()
        );
        Pagamento salvo = persistenceAdapter.salvar(primeiro);
        pagamentosCriados.add(salvo.getId());

        Pagamento duplicado = Pagamento.criarAguardandoPagamento(
                reservaId, primeiro.getSessaoId(), primeiro.getAssentoId(), primeiro.getClienteId(),
                new BigDecimal("45.00"), Instant.now()
        );
        Pagamento retornado = persistenceAdapter.salvar(duplicado);

        assertThat(retornado.getId()).isEqualTo(salvo.getId());
    }

    @Test
    @DisplayName("Deve lançar ObjectOptimisticLockingFailureException quando houver atualização concorrente com @Version desatualizada")
    void deveDetectarConflitoDeVersaoOtimista() {
        Pagamento inicial = persistenceAdapter.salvar(Pagamento.criarAguardandoPagamento(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("60.00"), Instant.now()
        ));
        pagamentosCriados.add(inicial.getId());

        Pagamento copiaA = persistenceAdapter.buscarPorId(inicial.getId()).orElseThrow();
        Pagamento copiaB = persistenceAdapter.buscarPorId(inicial.getId()).orElseThrow();

        copiaA.aprovar(MetodoPagamento.PIX, "PIX-111", Instant.now());
        persistenceAdapter.salvar(copiaA);

        copiaB.recusar(MetodoPagamento.CARTAO_CREDITO, "Recusa tardia", Instant.now());
        assertThatThrownBy(() -> persistenceAdapter.salvar(copiaB))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    @Test
    @DisplayName("Deve buscar apenas cobranças AGUARDANDO_PAGAMENTO vencidas via query nativa FOR UPDATE SKIP LOCKED")
    void deveBuscarApenasPendentesVencidos() {
        Instant agora = Instant.now();
        Pagamento vencido = persistenceAdapter.salvar(Pagamento.criarAguardandoPagamento(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("30.00"), agora.minus(Duration.ofMinutes(12))
        ));
        Pagamento valido = persistenceAdapter.salvar(Pagamento.criarAguardandoPagamento(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("30.00"), agora
        ));
        pagamentosCriados.add(vencido.getId());
        pagamentosCriados.add(valido.getId());

        List<Pagamento> encontrados = persistenceAdapter.buscarPendentesVencidos(agora, 100);

        assertThat(encontrados)
                .extracting(Pagamento::getId)
                .contains(vencido.getId())
                .doesNotContain(valido.getId());
        assertThat(encontrados)
                .allMatch(p -> p.getStatus() == StatusPagamento.AGUARDANDO_PAGAMENTO);
    }
}
