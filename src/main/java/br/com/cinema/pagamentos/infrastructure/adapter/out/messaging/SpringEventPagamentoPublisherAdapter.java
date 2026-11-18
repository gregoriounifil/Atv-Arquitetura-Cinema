package br.com.cinema.pagamentos.infrastructure.adapter.out.messaging;

import br.com.cinema.pagamentos.application.port.out.PublicarEventoPagamentoPort;
import br.com.cinema.shared.events.PagamentoAprovadoEvent;
import br.com.cinema.shared.events.PagamentoExpiradoEvent;
import br.com.cinema.shared.events.PagamentoRecusadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringEventPagamentoPublisherAdapter implements PublicarEventoPagamentoPort {

    private static final Logger log = LoggerFactory.getLogger(SpringEventPagamentoPublisherAdapter.class);

    private final ApplicationEventPublisher eventPublisher;

    public SpringEventPagamentoPublisherAdapter(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publicarPagamentoAprovado(PagamentoAprovadoEvent evento) {
        log.info("Publicando PagamentoAprovadoEvent no Outbox: pagamentoId={}, reservaId={}",
                evento.pagamentoId(), evento.reservaId());
        eventPublisher.publishEvent(evento);
    }

    @Override
    public void publicarPagamentoRecusado(PagamentoRecusadoEvent evento) {
        log.info("Publicando PagamentoRecusadoEvent no Outbox: pagamentoId={}, reservaId={}",
                evento.pagamentoId(), evento.reservaId());
        eventPublisher.publishEvent(evento);
    }

    @Override
    public void publicarPagamentoExpirado(PagamentoExpiradoEvent evento) {
        log.info("Publicando PagamentoExpiradoEvent no Outbox: pagamentoId={}, reservaId={}",
                evento.pagamentoId(), evento.reservaId());
        eventPublisher.publishEvent(evento);
    }
}
