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

        // 2. Crear Almacenes por defecto
        crearAlmacenesDefault(savedEmpresa);

        // 3. Crear Primer Administrador
        Usuario admin = new Usuario();
        admin.setUsuario(adminEmail); // Usamos el email como username por simplicidad o el que se provea
        admin.setNombre(adminNombre);
        admin.setApellidoPaterno("Admin");
        admin.setCorreo(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRol("ADMIN_EMPRESA");
        admin.setEmpresa(savedEmpresa);
        admin.setActivo(true);
        usuarioRepository.save(admin);

        // TODO: Enviar correo de bienvenida

        return savedEmpresa;
    }

    private void crearAlmacenesDefault(Empresa empresa) {
        String[] nombres = {"Stock", "Presupuesto", "Reportes"};
        for (int i = 0; i < nombres.length; i++) {
            Almacen almacen = new Almacen();
            // Generar ID único para almacén si no es auto-incremental
            // En el schema actual parece que ID de almacén es INT PRIMARY KEY (manual)
            // Necesito verificar si es SERIAL o no.
            almacen.setId(empresa.getId() * 100 + i + 1); 
            almacen.setNombre(nombres[i]);
            almacen.setUbicacion("Principal");
            almacen.setEmpresa(empresa);
            almacen.setActivo(true);
            almacenRepository.save(almacen);
        }
    }

    @Transactional
    public void delete(Integer id) {
        empresaRepository.deleteById(id);
    }
}
