package com.zentinel.demo.repositories;

import com.zentinel.demo.models.SalidaDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalidaDetalleRepository extends JpaRepository<SalidaDetalle, Integer> {

    @org.springframework.data.jpa.repository.Query("SELECT d.producto, SUM(d.cantidadEntregada) FROM SalidaDetalle d WHERE d.salida.empresa.id = :empresaId AND d.salida.cancelado = false GROUP BY d.producto ORDER BY SUM(d.cantidadEntregada) DESC")
    java.util.List<Object[]> findTopProductosDispatched(@org.springframework.data.repository.query.Param("empresaId") Integer empresaId, org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT d.salida.departamento, SUM(d.cantidadEntregada) FROM SalidaDetalle d WHERE d.salida.empresa.id = :empresaId AND d.producto.sku = :sku AND d.salida.cancelado = false AND d.salida.departamento IS NOT NULL GROUP BY d.salida.departamento ORDER BY SUM(d.cantidadEntregada) DESC")
    java.util.List<Object[]> findDepartamentosByProducto(@org.springframework.data.repository.query.Param("empresaId") Integer empresaId, @org.springframework.data.repository.query.Param("sku") String sku);
}
