package com.zentinel.demo.repositories;

import com.zentinel.demo.models.Entrada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntradaRepository extends JpaRepository<Entrada, Integer> {
    java.util.List<Entrada> findByEmpresa_Id(Integer empresaId);
    long countByEmpresa_IdAndFechaRecepcionAfter(Integer empresaId, java.time.LocalDateTime date);
}
