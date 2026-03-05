package com.zentinel.demo.repositories;

import com.zentinel.demo.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    // El "String" es porque el ID de tu tabla Usuario es el campo 'usuario' (texto)
}