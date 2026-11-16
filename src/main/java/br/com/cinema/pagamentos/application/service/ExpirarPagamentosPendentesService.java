package br.com.cinema.pagamentos.application.service;

import br.com.cinema.pagamentos.application.port.in.ExpirarPagamentosPendentesUseCase;
import br.com.cinema.pagamentos.application.port.out.BuscarPagamentoPort;
import br.com.cinema.pagamentos.application.port.out.PublicarEventoPagamentoPort;
import br.com.cinema.pagamentos.application.port.out.SalvarPagamentoPort;
import br.com.cinema.pagamentos.domain.model.Pagamento;
import br.com.cinema.shared.events.PagamentoExpiradoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class ExpirarPagamentosPendentesService implements ExpirarPagamentosPendentesUseCase {

    private static final Logger log = LoggerFactory.getLogger(ExpirarPagamentosPendentesService.class);
    private static final int TAMANHO_LOTE = 100;

    private final BuscarPagamentoPort buscarPagamentoPort;
    private final SalvarPagamentoPort salvarPagamentoPort;
    private final PublicarEventoPagamentoPort publicarEventoPagamentoPort;

    public ExpirarPagamentosPendentesService(
            BuscarPagamentoPort buscarPagamentoPort,
            SalvarPagamentoPort salvarPagamentoPort,
            PublicarEventoPagamentoPort publicarEventoPagamentoPort
    ) {
        this.buscarPagamentoPort = buscarPagamentoPort;
        this.salvarPagamentoPort = salvarPagamentoPort;
        this.publicarEventoPagamentoPort = publicarEventoPagamentoPort;
    }

    @Override
    @Transactional
    public int expirarPagamentosVencidos(Instant instanteReferencia) {
        List<Pagamento> vencidos = buscarPagamentoPort.buscarPendentesVencidos(instanteReferencia, TAMANHO_LOTE);
        for (Pagamento pagamento : vencidos) {
            pagamento.expirar(instanteReferencia);
            salvarPagamentoPort.salvar(pagamento);

            publicarEventoPagamentoPort.publicarPagamentoExpirado(new PagamentoExpiradoEvent(
                    pagamento.getId(),
                    pagamento.getReservaId(),
                    pagamento.getSessaoId(),
                    pagamento.getAssentoId(),
                    instanteReferencia
            ));
            log.info("Pagamento expirado por timeout de 10 min: pagamentoId={}, reservaId={}",
                    pagamento.getId(), pagamento.getReservaId());
        }
        return vencidos.size();
    }
}
