package com.zentinel.demo.models;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "entradas")
@Data
public class Entrada {
    @Column(name = "folio_factura", length = 50, nullable = false, unique = true)
    private String folioFactura;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    @Column(name = "fecha_factura", nullable = false)
    private LocalDateTime fechaFactura;

    @Column(name = "salida_relacionada_id")
    private Integer salidaRelacionadaId;

    @Column(name = "fecha_recepcion")
    private LocalDateTime fechaRecepcion;

    @Column(name = "num_pp", length = 50)
    private String numPp;

    @Column(length = 255)
    private String comentarios;

    @ManyToOne
    @JoinColumn(name = "almacen_destino_id")
    private Almacen almacenDestino;

    @Column(length = 20)
    private String estatus;

    @Column(precision = 14, scale = 2)
    private BigDecimal iva;

    @Column(precision = 14, scale = 2)
    private BigDecimal total;

    @Column(name = "tipo_cambio", precision = 8, scale = 4)
    private BigDecimal tipoCambio;

    @Column(nullable = false)
    private Boolean cancelado = false;

    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;

    @ManyToOne
    @JoinColumn(name = "usuario_cancelacion")
    private Usuario usuarioCancelacion;

    @Column(name = "motivo_cancelacion", length = 255)
    private String motivoCancelacion;

    @OneToMany(mappedBy = "entrada", cascade = CascadeType.ALL)
    private List<EntradaDetalle> detalles;

    @PrePersist
    protected void onCreate() {
        if (fechaRecepcion == null) {
            fechaRecepcion = LocalDateTime.now();
        }
    }
}
