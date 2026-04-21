package com.zentinel.demo.services;

import com.zentinel.demo.models.*;
import com.zentinel.demo.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EntradaService {

    private final EntradaRepository entradaRepository;
    private final EntradaDetalleRepository entradaDetalleRepository;
    private final InventarioRepository inventarioRepository;

    public EntradaService(EntradaRepository entradaRepository,
            EntradaDetalleRepository entradaDetalleRepository,
            InventarioRepository inventarioRepository) {
        this.entradaRepository = entradaRepository;
        this.entradaDetalleRepository = entradaDetalleRepository;
        this.inventarioRepository = inventarioRepository;
    }

    @Transactional
    public Entrada registrarEntrada(Entrada entrada, List<EntradaDetalle> detalles) {
        entrada.setFechaRecepcion(LocalDateTime.now());
        entrada.setFechaFactura(LocalDateTime.now()); // Ajustar si viene del form
        entrada.setEstatus("RECIBIDA");
        entrada.setCancelado(false);

        BigDecimal subtotal = BigDecimal.ZERO;
        for (EntradaDetalle detalle : detalles) {
            BigDecimal linea = detalle.getPrecioUnitario().multiply(detalle.getCantidad());
            detalle.setSubtotalLinea(linea);
            subtotal = subtotal.add(linea);
        }

        entrada.setTotal(subtotal);
        // IVA 16% simple
        entrada.setIva(subtotal.multiply(new BigDecimal("0.16")));
        entrada.setTotal(subtotal.add(entrada.getIva()));

        Entrada savedEntrada = entradaRepository.save(entrada);

        for (EntradaDetalle detalle : detalles) {
            detalle.setEntrada(savedEntrada);
            entradaDetalleRepository.save(detalle);

            // Actualizar Inventario
            Optional<Inventario> optInv = inventarioRepository.findByProductoAndAlmacen(
                    detalle.getProducto(), entrada.getAlmacenDestino());

            Inventario inv;
            if (optInv.isPresent()) {
                inv = optInv.get();
                inv.setCantidad(inv.getCantidad().add(detalle.getCantidad()));
            } else {
                inv = new Inventario();
                inv.setProducto(detalle.getProducto());
                inv.setAlmacen(entrada.getAlmacenDestino());
                inv.setCantidad(detalle.getCantidad());
                inv.setPuntoReorden(BigDecimal.ZERO);
            }
            inventarioRepository.save(inv);
        }

        return savedEntrada;
    }

    public List<Entrada> findAll() {
        return entradaRepository.findAll();
    }

    public Entrada findById(Integer id) {
        return entradaRepository.findById(id).orElse(null);
    }

    public List<Entrada> findByEmpresaId(Integer empresaId) {
        return entradaRepository.findByEmpresa_Id(empresaId);
    }

    public Entrada save(Entrada entrada) {
        return entradaRepository.save(entrada);
    }
}
