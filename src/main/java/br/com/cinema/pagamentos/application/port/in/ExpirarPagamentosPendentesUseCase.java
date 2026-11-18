package br.com.cinema.pagamentos.application.port.in;

import java.time.Instant;

public interface ExpirarPagamentosPendentesUseCase {
    int expirarPagamentosVencidos(Instant instanteReferencia);
}
