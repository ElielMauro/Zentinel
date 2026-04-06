package com.zentinel.demo.services;

import com.zentinel.demo.models.Producto;
import com.zentinel.demo.repositories.ProductoRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductoService {
    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> findAll() {
        return productoRepository.findAll();
    }

    public Producto findBySku(String sku) {
        return productoRepository.findById(sku).orElse(null);
    }

    public Producto save(Producto producto) {
        return productoRepository.save(producto);
    }

    public java.math.BigDecimal calcularPuntoReordenDinamico(String sku, int diasHistorico) {
        // Lógica para calcular punto de reorden basado en uso promedio
        // TODO: Consultar Salidas recientes para estimar demanda
        return new java.math.BigDecimal("10.00"); // Valor por defecto
    }

    public List<Producto> findByEmpresaId(Integer empresaId) {
        return productoRepository.findByEmpresa_Id(empresaId);
    }
}

