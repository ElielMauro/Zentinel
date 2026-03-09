package com.zentinel.demo.security;

import com.zentinel.demo.models.Usuario;
import com.zentinel.demo.services.UsuarioService;
import com.zentinel.demo.repositories.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    public DataInitializer(UsuarioRepository usuarioRepository, UsuarioService usuarioService) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (usuarioRepository.count() == 0) {
            Usuario admin = new Usuario();
            admin.setUsuario("admin");
            admin.setPassword("admin123");
            admin.setNombre("Administrador");
            admin.setApellidoPaterno("Zentinel");
            admin.setCorreo("admin@zentinel.com");
            admin.setRol("ADMIN");
            usuarioService.save(admin);
            System.out.println("Usuario por defecto creado: admin / admin123");
        }
    }
}
