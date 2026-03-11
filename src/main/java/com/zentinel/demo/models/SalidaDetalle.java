package com.zentinel.demo.models;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "salidas_detalle")
@Data
public class SalidaDetalle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "salida_folio")
    private Salida salida;

    @ManyToOne
    @JoinColumn(name = "producto_sku")
    private Producto producto;

    @Column(name = "cantidad_solicitada", nullable = false, precision = 12, scale = 4)
    private BigDecimal cantidadSolicitada;

    @Column(name = "cantidad_entregada", nullable = false, precision = 12, scale = 4)
    private BigDecimal cantidadEntregada;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "subtotal_linea", nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotalLinea;
}
