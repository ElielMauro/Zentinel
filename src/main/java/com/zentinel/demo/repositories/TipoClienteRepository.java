package com.zentinel.demo.repositories;

import com.zentinel.demo.models.TipoCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoClienteRepository extends JpaRepository<TipoCliente, Integer> {
}
