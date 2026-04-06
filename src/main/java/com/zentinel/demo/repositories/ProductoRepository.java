package com.zentinel.demo.repositories;

import com.zentinel.demo.models.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, String> {
    java.util.List<Producto> findByEmpresa_Id(Integer empresaId);
    long countByEmpresa_Id(Integer empresaId);
}
