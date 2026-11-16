package br.com.cinema.pagamentos.application.port.out;

import br.com.cinema.pagamentos.domain.model.Pagamento;

public interface SalvarPagamentoPort {
    Pagamento salvar(Pagamento pagamento);
}
