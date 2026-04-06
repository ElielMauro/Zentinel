package com.zentinel.demo.repositories;

import com.zentinel.demo.models.Salida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalidaRepository extends JpaRepository<Salida, Integer> {
    java.util.List<Salida> findByEmpresa_Id(Integer empresaId);
    long countByEmpresa_IdAndFechaAfter(Integer empresaId, java.time.LocalDateTime date);
    java.util.List<Salida> findByEmpresa_IdAndNumReporte(Integer empresaId, String numReporte);
}
