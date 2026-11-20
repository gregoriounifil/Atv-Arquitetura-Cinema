package br.com.cinema.catalogo.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface SpringDataSessaoRepository extends JpaRepository<SessaoJpaEntity, UUID> {
    List<SessaoJpaEntity> findAllByFilmeIdOrderByInicioAsc(UUID filmeId);
}
