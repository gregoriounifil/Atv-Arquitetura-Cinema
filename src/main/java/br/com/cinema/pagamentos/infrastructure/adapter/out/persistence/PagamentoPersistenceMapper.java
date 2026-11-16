package br.com.cinema.pagamentos.infrastructure.adapter.out.persistence;

import br.com.cinema.pagamentos.domain.model.Pagamento;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PagamentoPersistenceMapper {

    PagamentoJpaEntity toJpaEntity(Pagamento domain);

    default Pagamento toDomain(PagamentoJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Pagamento(
                entity.getId(),
                entity.getReservaId(),
                entity.getSessaoId(),
                entity.getAssentoId(),
                entity.getClienteId(),
                entity.getValor(),
                entity.getChaveIdempotencia(),
                entity.getStatus(),
                entity.getMetodoPagamento(),
                entity.getCodigoTransacaoGateway(),
                entity.getMotivoRecusa(),
                entity.getCriadoEm(),
                entity.getExpiraEm(),
                entity.getAtualizadoEm(),
                entity.getVersion()
        );
    }
}
