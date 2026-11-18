package br.com.cinema.pagamentos.application.port.out;

import br.com.cinema.pagamentos.domain.model.Pagamento;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BuscarPagamentoPort {
    Optional<Pagamento> buscarPorId(UUID pagamentoId);
    Optional<Pagamento> buscarPorReservaId(UUID reservaId);
    List<Pagamento> buscarPendentesVencidos(Instant instanteReferencia, int limite);
}
