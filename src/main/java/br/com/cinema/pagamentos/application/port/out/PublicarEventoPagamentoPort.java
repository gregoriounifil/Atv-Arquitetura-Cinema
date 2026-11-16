package br.com.cinema.pagamentos.application.port.out;

import br.com.cinema.shared.events.PagamentoAprovadoEvent;
import br.com.cinema.shared.events.PagamentoExpiradoEvent;
import br.com.cinema.shared.events.PagamentoRecusadoEvent;

public interface PublicarEventoPagamentoPort {
    void publicarPagamentoAprovado(PagamentoAprovadoEvent evento);
    void publicarPagamentoRecusado(PagamentoRecusadoEvent evento);
    void publicarPagamentoExpirado(PagamentoExpiradoEvent evento);
}
