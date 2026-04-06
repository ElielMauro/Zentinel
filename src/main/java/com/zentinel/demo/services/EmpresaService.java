package com.zentinel.demo.services;

import com.zentinel.demo.models.Almacen;
import com.zentinel.demo.models.Empresa;
import com.zentinel.demo.models.Usuario;
import com.zentinel.demo.repositories.AlmacenRepository;
import com.zentinel.demo.repositories.EmpresaRepository;
import com.zentinel.demo.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AlmacenRepository almacenRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Empresa> findAll() {
        return empresaRepository.findAll();
    }

    public Empresa findById(Integer id) {
        return empresaRepository.findById(id).orElse(null);
    }

    @Transactional
    public Empresa save(Empresa empresa, String adminNombre, String adminEmail, String adminPassword) {
        // 1. Guardar la empresa
        if (empresa.getFechaAlta() == null) {
            empresa.setFechaAlta(LocalDateTime.now());
        }
        Empresa savedEmpresa = empresaRepository.save(empresa);

        // 2. Crear Almacenes por defecto y capturarlos (Solo si no tiene ya)
        java.util.List<Almacen> defaultAlmacenes;
        if (savedEmpresa.getAlmacenes() == null || savedEmpresa.getAlmacenes().isEmpty()) {
            defaultAlmacenes = crearAlmacenesDefault(savedEmpresa);
        } else {
            defaultAlmacenes = new java.util.ArrayList<>(savedEmpresa.getAlmacenes());
        }

        // 3. Crear Primer Administrador
        Usuario admin = new Usuario();
        admin.setUsuario(adminEmail);
        admin.setNombre(adminNombre);
        admin.setApellidoPaterno("Admin");
        admin.setCorreo(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRol("ADMIN_EMPRESA");
        admin.setEmpresa(savedEmpresa);
        admin.setActivo(true);
        
        // 4. Vincular el admin a los almacenes creados por defecto
        admin.getAlmacenes().addAll(defaultAlmacenes);
        
        usuarioRepository.save(admin);

        return savedEmpresa;
    }

    private java.util.List<Almacen> crearAlmacenesDefault(Empresa empresa) {
        String[] nombres = {"Almacén General", "Bodega Secundaria", "Mermas"};
        java.util.List<Almacen> creados = new java.util.ArrayList<>();
        for (int i = 0; i < nombres.length; i++) {
            Almacen almacen = new Almacen();
            // Generar ID único basado en empresa_id y un offset
            almacen.setId(empresa.getId() * 100 + i + 1); 
            almacen.setNombre(nombres[i]);
            almacen.setUbicacion("Principal");
            almacen.setEmpresa(empresa);
            almacen.setActivo(true);
            creados.add(almacenRepository.save(almacen));
        }
        return creados;
    }

    @Transactional
    public void delete(Integer id) {
        empresaRepository.deleteById(id);
    }
}
