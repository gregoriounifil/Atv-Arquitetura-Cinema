package br.com.cinema.pagamentos.infrastructure.adapter.in.messaging;

import br.com.cinema.pagamentos.application.port.in.CriarCobrancaCommand;
import br.com.cinema.pagamentos.application.port.in.CriarCobrancaReservaUseCase;
import br.com.cinema.shared.events.AssentoReservadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AssentoReservadoEventListener {

    private static final Logger log = LoggerFactory.getLogger(AssentoReservadoEventListener.class);

    private final CriarCobrancaReservaUseCase criarCobrancaReservaUseCase;

    public AssentoReservadoEventListener(CriarCobrancaReservaUseCase criarCobrancaReservaUseCase) {
        this.criarCobrancaReservaUseCase = criarCobrancaReservaUseCase;
    }

    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onAssentoReservado(AssentoReservadoEvent event) {
        log.info("Recebido AssentoReservadoEvent para reservaId={}, sessaoId={}, assentoId={}",
                event.reservaId(), event.sessaoId(), event.assentoId());

        criarCobrancaReservaUseCase.executar(new CriarCobrancaCommand(
                event.reservaId(),
                event.sessaoId(),
                event.assentoId(),
                event.clienteId(),
                event.valor()
        ));
    }
}
