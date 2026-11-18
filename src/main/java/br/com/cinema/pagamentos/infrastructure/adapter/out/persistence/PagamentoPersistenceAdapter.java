package br.com.cinema.pagamentos.infrastructure.adapter.out.persistence;

import br.com.cinema.pagamentos.application.port.out.BuscarPagamentoPort;
import br.com.cinema.pagamentos.application.port.out.SalvarPagamentoPort;
import br.com.cinema.pagamentos.domain.model.Pagamento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PagamentoPersistenceAdapter implements SalvarPagamentoPort, BuscarPagamentoPort {

    private static final Logger log = LoggerFactory.getLogger(PagamentoPersistenceAdapter.class);

    private final SpringDataPagamentoRepository repository;
    private final PagamentoPersistenceMapper mapper;

    public PagamentoPersistenceAdapter(
            SpringDataPagamentoRepository repository,
            PagamentoPersistenceMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Pagamento salvar(Pagamento pagamento) {
        try {
            PagamentoJpaEntity entity = mapper.toJpaEntity(pagamento);
            PagamentoJpaEntity salvo = repository.saveAndFlush(entity);
            return mapper.toDomain(salvo);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Violacao de constraint de unicidade para reservaId={}. Retornando pagamento existente.",
                    pagamento.getReservaId());
            return repository.findByReservaId(pagamento.getReservaId())
                    .map(mapper::toDomain)
                    .orElseThrow(() -> ex);
        }
    }

    @Override
    public Optional<Pagamento> buscarPorId(UUID pagamentoId) {
        return repository.findById(pagamentoId).map(mapper::toDomain);
    }

    @Override
    public Optional<Pagamento> buscarPorReservaId(UUID reservaId) {
        return repository.findByReservaId(reservaId).map(mapper::toDomain);
    }

    @Override
    public List<Pagamento> buscarPendentesVencidos(Instant instanteReferencia, int limite) {
        return repository.buscarPendentesVencidosParaExpiracao(instanteReferencia, limite)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
