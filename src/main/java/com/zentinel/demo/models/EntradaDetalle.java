package com.zentinel.demo.models;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "entradas_detalle")
@Data
public class EntradaDetalle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "entrada_id")
    private Entrada entrada;

    @ManyToOne
    @JoinColumn(name = "producto_sku")
    private Producto producto;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "subtotal_linea", nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotalLinea;
}
