package com.zentinel.demo.repositories;

import com.zentinel.demo.models.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Integer> {
    // Aquí el ID es Integer
}