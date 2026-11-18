package br.com.cinema.reservas.infrastructure.adapter.out.messaging;

import br.com.cinema.reservas.application.port.out.PublicarEventoReservaPort;
import br.com.cinema.shared.events.AssentoReservadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class ReservaEventPublisherAdapter implements PublicarEventoReservaPort {

    private static final Logger log = LoggerFactory.getLogger(ReservaEventPublisherAdapter.class);

    private final ApplicationEventPublisher publisher;

    ReservaEventPublisherAdapter(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publicar(AssentoReservadoEvent evento) {
        log.info(">>> PUBLICANDO AssentoReservadoEvent: reservaId={}", evento.reservaId());
        publisher.publishEvent(evento);
    }
}
