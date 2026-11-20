package br.com.cinema.catalogo.application.port.out;

import br.com.cinema.shared.events.SessaoCriadaEvent;

/** Porta de saida: avisa os outros modulos do que aconteceu no catalogo. */
public interface PublicarEventoCatalogoPort {
    void publicar(SessaoCriadaEvent evento);
}
