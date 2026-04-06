package com.zentinel.demo.services;

import com.zentinel.demo.models.Usuario;
import com.zentinel.demo.repositories.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public Usuario save(Usuario usuario) {
        // Encriptar contraseña solo si es nuevo o se envía una nueva
        if (usuario.getPassword() != null && !usuario.getPassword().startsWith("$2a$")) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        if (usuario.getFechaAlta() == null) {
            usuario.setFechaAlta(LocalDateTime.now());
        }
        return usuarioRepository.save(usuario);
    }

    public Usuario findById(String username) {
        return usuarioRepository.findById(username).orElse(null);
    }

    public void delete(String username) {
        usuarioRepository.findById(username).ifPresent(u -> {
            if ("SUPER_ADMIN".equals(u.getRol())) {
                throw new RuntimeException("No se puede eliminar un actor base del sistema.");
            }
            u.setActivo(false);
            usuarioRepository.save(u);
        });
    }
    public java.util.List<Usuario> findByEmpresaId(Integer empresaId) {
        return usuarioRepository.findByEmpresa_Id(empresaId);
    }
}

