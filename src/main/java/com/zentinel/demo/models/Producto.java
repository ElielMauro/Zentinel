package com.zentinel.demo.models;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "productos")
@Data
public class Producto {
    @Id
    @Column(length = 50)
    private String sku;

    @Column(name = "codigo_interno", length = 50)
    private String codigoInterno;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(length = 50)
    private String marca;

    @Column(length = 50)
    private String modelo;

    @Column(name = "unidad_medida", nullable = false, length = 20)
    private String unidadMedida;

    @ManyToOne
    @JoinColumn(name = "tipo_producto_id")
    private TipoProducto tipoProducto;

    @Column(name = "tiempo_entrega_dias")
    private Integer tiempoEntregaDias;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "iva_porcentaje", precision = 4, scale = 2)
    private BigDecimal ivaPorcentaje;

    @Column(name = "imagen_url", length = 255)
    private String imagenUrl;

    @Column(nullable = false)
    private Boolean activo = true;
}
