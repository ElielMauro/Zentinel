package com.zentinel.zentinel.models;

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
}