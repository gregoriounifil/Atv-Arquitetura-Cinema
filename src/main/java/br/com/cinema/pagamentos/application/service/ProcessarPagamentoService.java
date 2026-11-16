package br.com.cinema.pagamentos.application.service;

import br.com.cinema.pagamentos.application.port.in.PagamentoResult;
import br.com.cinema.pagamentos.application.port.in.ProcessarPagamentoCommand;
import br.com.cinema.pagamentos.application.port.in.ProcessarPagamentoUseCase;
import br.com.cinema.pagamentos.application.port.out.BuscarPagamentoPort;
import br.com.cinema.pagamentos.application.port.out.GatewayCartaoPort;
import br.com.cinema.pagamentos.application.port.out.PublicarEventoPagamentoPort;
import br.com.cinema.pagamentos.application.port.out.RespostaGateway;
import br.com.cinema.pagamentos.application.port.out.SalvarPagamentoPort;
import br.com.cinema.pagamentos.domain.exception.PagamentoExpiradoException;
import br.com.cinema.pagamentos.domain.exception.PagamentoNaoEncontradoException;
import br.com.cinema.pagamentos.domain.exception.TransicaoEstadoPagamentoInvalidaException;
import br.com.cinema.pagamentos.domain.model.DadosCartao;
import br.com.cinema.pagamentos.domain.model.Pagamento;
import br.com.cinema.shared.events.PagamentoAprovadoEvent;
import br.com.cinema.shared.events.PagamentoExpiradoEvent;
import br.com.cinema.shared.events.PagamentoRecusadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ProcessarPagamentoService implements ProcessarPagamentoUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessarPagamentoService.class);

    private final BuscarPagamentoPort buscarPagamentoPort;
    private final SalvarPagamentoPort salvarPagamentoPort;
    private final GatewayCartaoPort gatewayCartaoPort;
    private final PublicarEventoPagamentoPort publicarEventoPagamentoPort;

    public ProcessarPagamentoService(
            BuscarPagamentoPort buscarPagamentoPort,
            SalvarPagamentoPort salvarPagamentoPort,
            GatewayCartaoPort gatewayCartaoPort,
            PublicarEventoPagamentoPort publicarEventoPagamentoPort
    ) {
        this.buscarPagamentoPort = buscarPagamentoPort;
        this.salvarPagamentoPort = salvarPagamentoPort;
        this.gatewayCartaoPort = gatewayCartaoPort;
        this.publicarEventoPagamentoPort = publicarEventoPagamentoPort;
    }

    @Override
    @Transactional(noRollbackFor = PagamentoExpiradoException.class)
    public PagamentoResult executar(ProcessarPagamentoCommand command) {
        Pagamento pagamento = buscarPagamentoPort.buscarPorId(command.pagamentoId())
                .orElseThrow(() -> new PagamentoNaoEncontradoException(command.pagamentoId()));

        if (pagamento.getStatus().isTerminal()) {
            throw new TransicaoEstadoPagamentoInvalidaException(pagamento.getStatus().name(), "PROCESSAR");
        }

        Instant agora = Instant.now();
        if (pagamento.estaVencidoEm(agora)) {
            pagamento.expirar(agora);
            salvarPagamentoPort.salvar(pagamento);
            publicarEventoPagamentoPort.publicarPagamentoExpirado(new PagamentoExpiradoEvent(
                    pagamento.getId(),
                    pagamento.getReservaId(),
                    pagamento.getSessaoId(),
                    pagamento.getAssentoId(),
                    agora
            ));
            throw new PagamentoExpiradoException(pagamento.getId(), pagamento.getExpiraEm());
        }

        DadosCartao dadosCartao = new DadosCartao(
                command.numeroCartao(),
                command.nomeTitular(),
                command.validade(),
                command.cvv()
        );

        RespostaGateway resposta = gatewayCartaoPort.processarCobranca(
                pagamento.getId(),
                pagamento.getValor(),
                dadosCartao
        );

        Instant instantePosGateway = Instant.now();
        if (resposta.aprovado()) {
            pagamento.aprovar(command.metodoPagamento(), resposta.codigoTransacao(), instantePosGateway);
            Pagamento salvo = salvarPagamentoPort.salvar(pagamento);
            publicarEventoPagamentoPort.publicarPagamentoAprovado(new PagamentoAprovadoEvent(
                    salvo.getId(),
                    salvo.getReservaId(),
                    salvo.getSessaoId(),
                    salvo.getAssentoId(),
                    salvo.getCodigoTransacaoGateway(),
                    instantePosGateway
            ));
            log.info("Pagamento aprovado: pagamentoId={}, reservaId={}, finalCartao={}",
                    salvo.getId(), salvo.getReservaId(), dadosCartao.obterUltimosQuatroDigitos());
            return PagamentoResult.fromDomain(salvo);
        } else {
            pagamento.recusar(command.metodoPagamento(), resposta.motivoRecusa(), instantePosGateway);
            Pagamento salvo = salvarPagamentoPort.salvar(pagamento);
            publicarEventoPagamentoPort.publicarPagamentoRecusado(new PagamentoRecusadoEvent(
                    salvo.getId(),
                    salvo.getReservaId(),
                    salvo.getSessaoId(),
                    salvo.getAssentoId(),
                    salvo.getMotivoRecusa(),
                    instantePosGateway
            ));
            log.warn("Pagamento recusado: pagamentoId={}, reservaId={}, motivo={}",
                    salvo.getId(), salvo.getReservaId(), salvo.getMotivoRecusa());
            return PagamentoResult.fromDomain(salvo);
        }
    }
}
