package br.com.cinema.reservas.domain;

public enum StatusReserva {
    PENDENTE(false),
    CONFIRMADA(true),
    CANCELADA(true);

    private final boolean terminal;

    StatusReserva(boolean terminal) {
        this.terminal = terminal;
    }

    public boolean isTerminal() {
        return terminal;
    }
}
