package com.zentinel.demo.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "proveedores")
@Data
public class Proveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre_corto", nullable = false, length = 50)
    private String nombreCorto;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Column(length = 13)
    private String rfc;

    @Column(length = 255)
    private String direccion;

    @Column(length = 20)
    private String telefono;

    @Column(length = 100)
    private String contacto;

    @Column(length = 100)
    private String correo;

    @Column(nullable = false)
    private Boolean activo = true;
}
