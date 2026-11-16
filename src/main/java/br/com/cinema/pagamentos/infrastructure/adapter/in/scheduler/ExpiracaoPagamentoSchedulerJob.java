package br.com.cinema.pagamentos.infrastructure.adapter.in.scheduler;

import br.com.cinema.pagamentos.application.port.in.ExpirarPagamentosPendentesUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ExpiracaoPagamentoSchedulerJob {

    private final ExpirarPagamentosPendentesUseCase expirarPagamentosPendentesUseCase;

    public ExpiracaoPagamentoSchedulerJob(ExpirarPagamentosPendentesUseCase expirarPagamentosPendentesUseCase) {
        this.expirarPagamentosPendentesUseCase = expirarPagamentosPendentesUseCase;
    }

    @Scheduled(fixedDelayString = "${cinema.pagamentos.expiracao.fixed-delay-ms:15000}")
    public void verificarPagamentosVencidos() {
        expirarPagamentosPendentesUseCase.expirarPagamentosVencidos(Instant.now());
    }
}
