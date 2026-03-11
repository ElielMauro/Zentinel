package com.zentinel.demo.repositories;

import com.zentinel.demo.models.Salida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalidaRepository extends JpaRepository<Salida, Integer> {
}
