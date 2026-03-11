package com.zentinel.demo.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tipo_producto")
@Data
public class TipoProducto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(length = 200)
    private String descripcion;
}
