package br.com.cinema.pagamentos.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataPagamentoRepository extends JpaRepository<PagamentoJpaEntity, UUID> {

    Optional<PagamentoJpaEntity> findByReservaId(UUID reservaId);

    @Query(value = """
            SELECT *
              FROM pagamentos
             WHERE status = 'AGUARDANDO_PAGAMENTO'
               AND expira_em <= :instanteReferencia
             ORDER BY expira_em ASC, criado_em ASC
             LIMIT :limite
               FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<PagamentoJpaEntity> buscarPendentesVencidosParaExpiracao(
            @Param("instanteReferencia") Instant instanteReferencia,
            @Param("limite") int limite
    );
}
