package br.com.cinema.reservas.application.port.out;

import br.com.cinema.shared.events.AssentoReservadoEvent;

public interface PublicarEventoReservaPort {
    void publicar(AssentoReservadoEvent evento);
}
