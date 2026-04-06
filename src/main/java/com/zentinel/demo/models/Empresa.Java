package com.zentinel.demo.models;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "empresa")
@Data
public class Empresa {

    @Id
    private Integer id; // ID manual según el diagrama

    @Column(nullable = false, length = 20)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "nombre_completo", length = 200)
    private String nombreCompleto;

    @Column(nullable = false, length = 13)
    private String rfc;

    @Column(length = 50)
    private String plan;

    @Column(name = "contacto_principal", length = 100)
    private String contactoPrincipal;

    @Column(name = "email_contacto", length = 100)
    private String emailContacto;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_alta")
    private java.time.LocalDateTime fechaAlta = java.time.LocalDateTime.now();

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<Almacen> almacenes;
}
