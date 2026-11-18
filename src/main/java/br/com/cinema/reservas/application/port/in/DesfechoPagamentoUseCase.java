package br.com.cinema.reservas.application.port.in;

import java.util.UUID;

/** Porta de entrada acionada pelos eventos vindos do modulo Pagamentos. */
public interface DesfechoPagamentoUseCase {
    void confirmar(UUID reservaId);
    void cancelar(UUID reservaId, String motivo);
}
