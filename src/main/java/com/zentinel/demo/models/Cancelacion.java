package com.zentinel.demo.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "cancelaciones")
@Data
public class Cancelacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tipo_documento", nullable = false, length = 20)
    private String tipoDocumento;

    @Column(name = "documento_id", nullable = false)
    private Integer documentoId;

    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;

    @ManyToOne
    @JoinColumn(name = "usuario_cancelacion")
    private Usuario usuarioCancelacion;

    @Column(nullable = false, length = 255)
    private String motivo;

    @PrePersist
    protected void onCreate() {
        if (fechaCancelacion == null) {
            fechaCancelacion = LocalDateTime.now();
        }
    }
}
