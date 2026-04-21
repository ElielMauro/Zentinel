package com.zentinel.demo.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "clientes")
@Data
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 20)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 13)
    private String rfc;

    @Column(length = 18)
    private String curp;

    @Column(length = 20)
    private String telefono;

    @Column(name = "contacto_adicional", length = 100)
    private String contactoAdicional;

    @Column(length = 100)
    private String correo;

    @ManyToOne
    @JoinColumn(name = "tipo_cliente_id")
    private TipoCliente tipoCliente;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @PrePersist
    protected void onCreate() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
    }
}
