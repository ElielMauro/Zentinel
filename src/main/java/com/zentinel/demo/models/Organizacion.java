package com.zentinel.demo.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "organizacion")
@Data
public class Organizacion {
    @Id
    private Integer id;

    @Column(name = "nombre_corto", nullable = false, length = 50)
    private String nombreCorto;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Column(length = 50)
    private String departamento;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;
}
