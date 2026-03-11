package com.zentinel.demo.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @Column(name = "usuario", length = 50)
    private String usuario;

    @Column(length = 255, nullable = false)
    private String password;

    @Column(length = 50, nullable = false)
    private String nombre;

    @Column(name = "apellido_paterno", length = 50, nullable = false)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", length = 50)
    private String apellidoMaterno;

    @Column(length = 100, nullable = false)
    private String correo;

    @Column(length = 20, nullable = false)
    private String rol;

    @Column(name = "fecha_alta", updatable = false)
    private LocalDateTime fechaAlta;

    @Column(nullable = false)
    private Boolean activo = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "usuario_almacen", joinColumns = @JoinColumn(name = "usuario"), inverseJoinColumns = @JoinColumn(name = "almacen_id"))
    private Set<Almacen> almacenes = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (fechaAlta == null) {
            fechaAlta = LocalDateTime.now();
        }
    }
}
