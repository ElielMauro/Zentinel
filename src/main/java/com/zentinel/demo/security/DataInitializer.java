package com.zentinel.demo.security;

import com.zentinel.demo.models.*;
import com.zentinel.demo.services.UsuarioService;
import com.zentinel.demo.repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final AlmacenRepository almacenRepository;
    private final AreaRepository areaRepository;
    private final UsuarioService usuarioService;

    public DataInitializer(UsuarioRepository usuarioRepository,
            EmpresaRepository empresaRepository,
            AlmacenRepository almacenRepository,
            AreaRepository areaRepository,
            UsuarioService usuarioService) {
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.almacenRepository = almacenRepository;
        this.areaRepository = areaRepository;
        this.usuarioService = usuarioService;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Crear Empresa si no existe
        if (empresaRepository.count() == 0) {
            Empresa emp = new Empresa();
            emp.setId(1);
            emp.setCodigo("ZENT01");
            emp.setNombre("Zentinel S.A.");
            emp.setNombreCompleto("Zentinel Logística S.A. de C.V.");
            emp.setRfc("ZENT010101ABC");
            empresaRepository.save(emp);
        }

        // 2. Crear Almacén si no existe
        if (almacenRepository.count() == 0) {
            Almacen alm = new Almacen();
            alm.setId(1);
            alm.setNombre("Almacén Central");
            alm.setUbicacion("Planta Principal");
            alm.setEmpresa(empresaRepository.findById(1).orElse(null));
            almacenRepository.save(alm);
        }

        // 3. Crear Área si no existe
        if (areaRepository.count() == 0) {
            Area area = new Area();
            area.setNombre("Mantenimiento");
            area.setDescripcion("Departamento de mantenimiento industrial");
            areaRepository.save(area);
        }

        // 4. Crear Usuarios
        if (usuarioRepository.count() == 0) {
            // Admin
            Usuario admin = new Usuario();
            admin.setUsuario("admin");
            admin.setPassword("admin123");
            admin.setNombre("Administrador");
            admin.setApellidoPaterno("Zentinel");
            admin.setCorreo("admin@zentinel.com");
            admin.setRol("ADMIN");
            usuarioService.save(admin);

            // Mostrador
            Usuario mostrador = new Usuario();
            mostrador.setUsuario("mostrador");
            mostrador.setPassword("mostrador123");
            mostrador.setNombre("Operador");
            mostrador.setApellidoPaterno("Ventas");
            mostrador.setCorreo("ventas@zentinel.com");
            mostrador.setRol("MOSTRADOR");
            // Asignar al almacén 1
            mostrador.getAlmacenes().add(almacenRepository.findById(1).orElse(null));
            usuarioService.save(mostrador);

            System.out.println("Usuarios de prueba creados.");
        }
    }
}
