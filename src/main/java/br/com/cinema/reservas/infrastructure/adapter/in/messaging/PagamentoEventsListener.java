package br.com.cinema.reservas.infrastructure.adapter.in.messaging;

import br.com.cinema.reservas.application.port.in.DesfechoPagamentoUseCase;
import br.com.cinema.shared.events.PagamentoAprovadoEvent;
import br.com.cinema.shared.events.PagamentoExpiradoEvent;
import br.com.cinema.shared.events.PagamentoRecusadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Fecha o ciclo event-driven: Reservas publica, Pagamentos responde,
 * Reservas reage. Nenhum dos dois modulos importa classes do outro -
 * a conversa acontece so pelos contratos em shared/events.
 */
@Component
public class PagamentoEventsListener {

    private static final Logger log = LoggerFactory.getLogger(PagamentoEventsListener.class);

    private final DesfechoPagamentoUseCase desfecho;

    PagamentoEventsListener(DesfechoPagamentoUseCase desfecho) {
        this.desfecho = desfecho;
    }

    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onAprovado(PagamentoAprovadoEvent e) {
        log.info("<<< RECEBIDO PagamentoAprovadoEvent: reservaId={}", e.reservaId());
        desfecho.confirmar(e.reservaId());
    }

    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onRecusado(PagamentoRecusadoEvent e) {
        log.info("<<< RECEBIDO PagamentoRecusadoEvent: reservaId={}", e.reservaId());
        desfecho.cancelar(e.reservaId(), "Pagamento recusado: " + e.motivo());
    }

    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onExpirado(PagamentoExpiradoEvent e) {
        log.info("<<< RECEBIDO PagamentoExpiradoEvent: reservaId={}", e.reservaId());
        desfecho.cancelar(e.reservaId(), "Prazo de 10 minutos para pagamento expirou.");
    }
}
