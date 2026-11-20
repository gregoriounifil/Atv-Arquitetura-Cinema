package br.com.cinema.catalogo.infrastructure.adapter.out.messaging;

import br.com.cinema.catalogo.application.port.out.PublicarEventoCatalogoPort;
import br.com.cinema.shared.events.SessaoCriadaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class CatalogoEventPublisherAdapter implements PublicarEventoCatalogoPort {

    private static final Logger log = LoggerFactory.getLogger(CatalogoEventPublisherAdapter.class);

    private final ApplicationEventPublisher publisher;

    CatalogoEventPublisherAdapter(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publicar(SessaoCriadaEvent evento) {
        log.info(">>> PUBLICANDO SessaoCriadaEvent: sessaoId={}", evento.sessaoId());
        publisher.publishEvent(evento);
    }
}
