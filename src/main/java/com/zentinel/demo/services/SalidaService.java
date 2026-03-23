package com.zentinel.demo.services;

import com.zentinel.demo.models.*;
import com.zentinel.demo.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SalidaService {

    private final SalidaRepository salidaRepository;
    private final SalidaDetalleRepository salidaDetalleRepository;
    private final InventarioRepository inventarioRepository;

    public SalidaService(SalidaRepository salidaRepository,
            SalidaDetalleRepository salidaDetalleRepository,
            InventarioRepository inventarioRepository) {
        this.salidaRepository = salidaRepository;
        this.salidaDetalleRepository = salidaDetalleRepository;
        this.inventarioRepository = inventarioRepository;
    }

    @Transactional
    public Salida registrarSalida(Salida salida, List<SalidaDetalle> detalles) {
        salida.setFecha(LocalDateTime.now());
        salida.setEstatus("COMPLETADA");

        // Calcular totales
        BigDecimal subtotal = BigDecimal.ZERO;
        for (SalidaDetalle detalle : detalles) {
            BigDecimal linea = detalle.getPrecioUnitario().multiply(detalle.getCantidadEntregada());
            detalle.setSubtotalLinea(linea);
            subtotal = subtotal.add(linea);
        }
        salida.setSubtotal(subtotal);
        // IVA por defecto 16% o según producto, aquí simple 0 por ahora o cálculo base
        salida.setIva(subtotal.multiply(new BigDecimal("0.16")));
        salida.setTotal(subtotal.add(salida.getIva()));

        Salida savedSalida = salidaRepository.save(salida);

        for (SalidaDetalle detalle : detalles) {
            detalle.setSalida(savedSalida);
            salidaDetalleRepository.save(detalle);

            // Actualizar Inventario
            Inventario inv = inventarioRepository
                    .findByProductoAndAlmacen(detalle.getProducto(), salida.getAlmacenOrigen())
                    .orElseThrow(() -> new RuntimeException("No hay stock disponible para el producto "
                            + detalle.getProducto().getSku() + " en este almacén"));

            if (inv.getCantidad().compareTo(detalle.getCantidadEntregada()) < 0) {
                throw new RuntimeException("Stock insuficiente para " + detalle.getProducto().getNombre());
            }

            inv.setCantidad(inv.getCantidad().subtract(detalle.getCantidadEntregada()));
            inventarioRepository.save(inv);
        }

        return savedSalida;
    }

    public List<Salida> findAll() {
        return salidaRepository.findAll();
    }

    public Salida findByFolio(Integer folio) {
        return salidaRepository.findById(folio).orElse(null);
    }

    public List<Inventario> getInventarioByAlmacen(Almacen almacen) {
        return inventarioRepository.findByAlmacen(almacen);
    }

    public BigDecimal getStockComprometido(Producto producto, Almacen almacen) {
        // Sumar cantidades de salidas que NO están completadas ni canceladas
        List<Salida> salidasPendientes = salidaRepository.findAll().stream()
                .filter(s -> !s.getCancelado() && "RESERVADA".equals(s.getEstatus()) && s.getAlmacenOrigen() != null && s.getAlmacenOrigen().equals(almacen))
                .collect(Collectors.toList());

        BigDecimal comprometido = BigDecimal.ZERO;
        for (Salida s : salidasPendientes) {
            for (SalidaDetalle d : s.getDetalles()) {
                if (d.getProducto().getSku().equals(producto.getSku())) {
                    comprometido = comprometido.add(d.getCantidadSolicitada());
                }
            }
        }
        return comprometido;
    }

    @Transactional
    public Salida reservarSalida(Salida salida, List<SalidaDetalle> detalles) {
        salida.setFecha(LocalDateTime.now());
        salida.setEstatus("RESERVADA");
        salida.setCancelado(false);

        BigDecimal subtotal = BigDecimal.ZERO;
        for (SalidaDetalle detalle : detalles) {
            BigDecimal linea = detalle.getPrecioUnitario().multiply(detalle.getCantidadSolicitada());
            detalle.setSubtotalLinea(linea);
            subtotal = subtotal.add(linea);
        }
        salida.setSubtotal(subtotal);
        salida.setIva(subtotal.multiply(new BigDecimal("0.16")));
        salida.setTotal(subtotal.add(salida.getIva()));

        Salida savedSalida = salidaRepository.save(salida);
        for (SalidaDetalle detalle : detalles) {
            detalle.setSalida(savedSalida);
            salidaDetalleRepository.save(detalle);
        }
        return savedSalida;
    }
}
