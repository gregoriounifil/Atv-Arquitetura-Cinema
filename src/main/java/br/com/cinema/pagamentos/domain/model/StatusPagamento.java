package br.com.cinema.pagamentos.domain.model;

public enum StatusPagamento {
    AGUARDANDO_PAGAMENTO(false),
    APROVADO(true),
    RECUSADO(true),
    EXPIRADO(true);

    private final boolean terminal;

    StatusPagamento(boolean terminal) {
        this.terminal = terminal;
    }

    public boolean isTerminal() {
        return terminal;
    }
}
