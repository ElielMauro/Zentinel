package com.zentinel.demo.repositories;

import com.zentinel.demo.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    Optional<Usuario> findByUsuarioAndActivoTrue(String usuario);

    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.empresa WHERE u.usuario = :username AND u.activo = true")
    Optional<Usuario> findByUsuarioActivoWithEmpresa(@Param("username") String username);

    java.util.List<Usuario> findByEmpresa_Id(Integer empresaId);
}
