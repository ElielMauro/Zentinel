package com.zentinel.demo.repositories;

import com.zentinel.demo.models.EntradaDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntradaDetalleRepository extends JpaRepository<EntradaDetalle, Integer> {
}
