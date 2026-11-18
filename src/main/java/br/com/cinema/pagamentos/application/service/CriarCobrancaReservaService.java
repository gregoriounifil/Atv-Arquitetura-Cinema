package br.com.cinema.pagamentos.application.service;

import br.com.cinema.pagamentos.application.port.in.CriarCobrancaCommand;
import br.com.cinema.pagamentos.application.port.in.CriarCobrancaReservaUseCase;
import br.com.cinema.pagamentos.application.port.in.PagamentoResult;
import br.com.cinema.pagamentos.application.port.out.BuscarPagamentoPort;
import br.com.cinema.pagamentos.application.port.out.SalvarPagamentoPort;
import br.com.cinema.pagamentos.domain.model.Pagamento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class CriarCobrancaReservaService implements CriarCobrancaReservaUseCase {

    private static final Logger log = LoggerFactory.getLogger(CriarCobrancaReservaService.class);

    private final BuscarPagamentoPort buscarPagamentoPort;
    private final SalvarPagamentoPort salvarPagamentoPort;

    public CriarCobrancaReservaService(
            BuscarPagamentoPort buscarPagamentoPort,
            SalvarPagamentoPort salvarPagamentoPort
    ) {
        this.buscarPagamentoPort = buscarPagamentoPort;
        this.salvarPagamentoPort = salvarPagamentoPort;
    }

    @Override
    @Transactional
    public PagamentoResult executar(CriarCobrancaCommand command) {
        Optional<Pagamento> existente = buscarPagamentoPort.buscarPorReservaId(command.reservaId());
        if (existente.isPresent()) {
            log.info("[IDEMPOTENCIA] Cobranca ja existente para reservaId={}. Ignorando duplicata.",
                    command.reservaId());
            return PagamentoResult.fromDomain(existente.get());
        }

        Pagamento novoPagamento = Pagamento.criarAguardandoPagamento(
                command.reservaId(),
                command.sessaoId(),
                command.assentoId(),
                command.clienteId(),
                command.valor(),
                Instant.now()
        );

        Pagamento salvo = salvarPagamentoPort.salvar(novoPagamento);
        log.info("Cobranca criada com sucesso: pagamentoId={}, reservaId={}, expiraEm={}",
                salvo.getId(), salvo.getReservaId(), salvo.getExpiraEm());
        return PagamentoResult.fromDomain(salvo);
    }
}
