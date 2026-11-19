package br.com.cinema.catalogo.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface SpringDataSalaRepository extends JpaRepository<SalaJpaEntity, UUID> {
    List<SalaJpaEntity> findAllByOrderByNomeAsc();
}
