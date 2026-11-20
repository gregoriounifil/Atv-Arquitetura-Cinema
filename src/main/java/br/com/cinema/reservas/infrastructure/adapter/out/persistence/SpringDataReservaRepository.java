package br.com.cinema.reservas.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface SpringDataReservaRepository extends JpaRepository<ReservaJpaEntity, UUID> {
}
