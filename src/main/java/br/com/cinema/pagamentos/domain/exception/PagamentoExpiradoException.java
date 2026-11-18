package br.com.cinema.pagamentos.domain.exception;

import java.time.Instant;
import java.util.UUID;

public class PagamentoExpiradoException extends PagamentoDomainException {
    public PagamentoExpiradoException(UUID pagamentoId, Instant expiraEm) {
        super("O pagamento [%s] expirou em [%s] (limite de 10 minutos excedido)."
                .formatted(pagamentoId, expiraEm));
    }
}
