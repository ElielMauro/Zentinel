package com.zentinel.demo.repositories;

import com.zentinel.demo.models.Inventario;
import com.zentinel.demo.models.Almacen;
import com.zentinel.demo.models.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Integer> {
    List<Inventario> findByAlmacen(Almacen almacen);

    Optional<Inventario> findByProductoAndAlmacen(Producto producto, Almacen almacen);

    List<Inventario> findByAlmacen_Empresa_Id(Integer empresaId);
}

