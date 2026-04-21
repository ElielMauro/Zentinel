package com.zentinel.demo.models;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "salidas")
@Data
public class Salida {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer folio;

    @Column(name = "folio_formateado", length = 50, unique = true)
    private String folioFormateado;


    @Column(name = "fecha")
    private LocalDateTime fecha;

    @ManyToOne
    @JoinColumn(name = "usuario_solicitante")
    private Usuario usuarioSolicitante;

    @ManyToOne
    @JoinColumn(name = "usuario_atendio")
    private Usuario usuarioAtendio;

    @ManyToOne
    @JoinColumn(name = "area_id")
    private Area area;

    @Column(length = 100)
    private String departamento;

    @Column(name = "n_reporte", length = 50)
    private String numReporte;

    @Column(name = "num_presupuesto", length = 50)
    private String numPresupuesto;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "nombre_solicitante", length = 100)
    private String nombreSolicitante;

    @Column(name = "tipo_cliente", length = 50)
    private String tipoCliente;

    @Column(length = 20)
    private String estatus;

    @ManyToOne
    @JoinColumn(name = "almacen_origen_id")
    private Almacen almacenOrigen;

    @Column(precision = 14, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 14, scale = 2)
    private BigDecimal iva;

    @Column(precision = 14, scale = 2)
    private BigDecimal total;

    @Column(nullable = false)
    private Boolean cancelado = false;

    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;

    @ManyToOne
    @JoinColumn(name = "usuario_cancelacion")
    private Usuario usuarioCancelacion;

    @Column(name = "motivo_cancelacion", length = 255)
    private String motivoCancelacion;

    @OneToMany(mappedBy = "salida", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalidaDetalle> detalles;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @Column(nullable = false)
    private Boolean facturado = false;

    @PrePersist
    protected void onCreate() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }
}
