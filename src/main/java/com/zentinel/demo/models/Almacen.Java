package com.zentinel.demo.models; // Usando tu ruta actual

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "almacenes")
@Data
public class Almacen {

    @Id
    private Integer id; // Según tu PDF es un INT manual

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 200)
    private String ubicacion;

    @Column(name = "activo")
    private Boolean activo = true;

    // RELACIÓN: Muchos almacenes pertenecen a una Empresa
    @ManyToOne
    @JoinColumn(name = "empresa_id", referencedColumnName = "id")
    private Empresa empresa;
}