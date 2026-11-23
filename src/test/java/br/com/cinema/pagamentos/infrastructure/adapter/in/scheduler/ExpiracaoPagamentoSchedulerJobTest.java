package br.com.cinema.pagamentos.infrastructure.adapter.in.scheduler;

import br.com.cinema.pagamentos.application.port.in.ExpirarPagamentosPendentesUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários do Scheduler - ExpiracaoPagamentoSchedulerJob")
class ExpiracaoPagamentoSchedulerJobTest {

    @Mock
    private ExpirarPagamentosPendentesUseCase useCase;

    @InjectMocks
    private ExpiracaoPagamentoSchedulerJob job;

    @Test
    @DisplayName("Deve acionar ExpirarPagamentosPendentesUseCase com instante atual")
    void deveAcionarUseCaseDeExpiracao() {
        job.verificarPagamentosVencidos();

        verify(useCase).expirarPagamentosVencidos(any(Instant.class));
    }
}
