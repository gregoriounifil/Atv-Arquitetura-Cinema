package br.com.cinema.pagamentos.application.service;

import br.com.cinema.pagamentos.application.port.in.ConsultarPagamentoUseCase;
import br.com.cinema.pagamentos.application.port.in.PagamentoResult;
import br.com.cinema.pagamentos.application.port.out.BuscarPagamentoPort;
import br.com.cinema.pagamentos.domain.exception.PagamentoNaoEncontradoException;
import br.com.cinema.pagamentos.domain.model.Pagamento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ConsultarPagamentoService implements ConsultarPagamentoUseCase {

    private final BuscarPagamentoPort buscarPagamentoPort;

    public ConsultarPagamentoService(BuscarPagamentoPort buscarPagamentoPort) {
        this.buscarPagamentoPort = buscarPagamentoPort;
    }

    @Override
    public PagamentoResult buscarPorId(UUID pagamentoId) {
        Pagamento pagamento = buscarPagamentoPort.buscarPorId(pagamentoId)
                .orElseThrow(() -> new PagamentoNaoEncontradoException(pagamentoId));
        return PagamentoResult.fromDomain(pagamento);
    }

    @Override
    public PagamentoResult buscarPorReservaId(UUID reservaId) {
        Pagamento pagamento = buscarPagamentoPort.buscarPorReservaId(reservaId)
                .orElseThrow(() -> new PagamentoNaoEncontradoException(
                        "Pagamento nao encontrado para a reserva [%s].".formatted(reservaId)));
        return PagamentoResult.fromDomain(pagamento);
    }
}
