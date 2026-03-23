package com.zentinel.demo.repositories;

import com.zentinel.demo.models.Entrada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntradaRepository extends JpaRepository<Entrada, Integer> {
    long countByFechaRecepcionAfter(java.time.LocalDateTime date);
}
