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
    private final TipoClienteRepository tipoClienteRepository;

    public DataInitializer(UsuarioRepository usuarioRepository,
            EmpresaRepository empresaRepository,
            AlmacenRepository almacenRepository,
            AreaRepository areaRepository,
            UsuarioService usuarioService,
            TipoClienteRepository tipoClienteRepository) {
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.almacenRepository = almacenRepository;
        this.areaRepository = areaRepository;
        this.usuarioService = usuarioService;
        this.tipoClienteRepository = tipoClienteRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Crear Empresa si no existe
        Empresa emp = empresaRepository.findById(1).orElse(null);
        if (emp == null) {
            emp = new Empresa();
            emp.setId(1);
            emp.setCodigo("ZENT01");
            emp.setNombre("Zentinel S.A.");
            emp.setNombreCompleto("Zentinel Logística S.A. de C.V.");
            emp.setRfc("ZENT010101ABC");
            emp.setActivo(true);
            empresaRepository.save(emp);
        }

        // 2. Crear Almacenes por defecto
        if (almacenRepository.count() == 0) {
            String[] almacenes = {"Stock General", "Presupuesto", "Reportes y Mermas"};
            for (int i = 0; i < almacenes.length; i++) {
                Almacen alm = new Almacen();
                alm.setId(i + 1);
                alm.setNombre(almacenes[i]);
                alm.setUbicacion("Planta Principal");
                alm.setEmpresa(emp);
                almacenRepository.save(alm);
            }
        }

        // 3. Crear 9 Áreas funcionales
        if (areaRepository.count() == 0) {
            String[] areas = {"Mantenimiento", "Producción", "Ventas", "Recursos Humanos", "Sistemas", "Logística", "Calidad", "Dirección", "Almacén Central"};
            for (String nombreArea : areas) {
                Area area = new Area();
                area.setNombre(nombreArea);
                area.setDescripcion("Departamento de " + nombreArea);
                area.setEmpresa(emp);
                areaRepository.save(area);
            }
        }

        // 4. Crear Usuarios
        if (usuarioRepository.count() == 0) {
            Usuario superAdmin = new Usuario();
            superAdmin.setUsuario("superadmin");
            superAdmin.setPassword("Zentinel2024!");
            superAdmin.setNombre("Super");
            superAdmin.setApellidoPaterno("Admin");
            superAdmin.setCorreo("master@zentinel.com");
            superAdmin.setRol("SUPER_ADMIN");
            superAdmin.setEmpresa(emp);
            usuarioService.save(superAdmin);

            Usuario adminEmpresa = new Usuario();
            adminEmpresa.setUsuario("admin");
            adminEmpresa.setPassword("admin123");
            adminEmpresa.setNombre("Administrador");
            adminEmpresa.setApellidoPaterno("Empresa");
            adminEmpresa.setCorreo("admin@zentinel.com");
            adminEmpresa.setRol("ADMIN_EMPRESA");
            adminEmpresa.setEmpresa(emp);
            usuarioService.save(adminEmpresa);

            System.out.println("Base de datos inicializada para pruebas reales.");
        }

        // 5. Crear Tipo de Cliente por defecto si no hay ninguno
        if (tipoClienteRepository.count() == 0) {
            TipoCliente tc = new TipoCliente();
            tc.setNombre("General");
            tc.setDescripcion("Cliente estándar");
            tc.setMargenUtilidad(new java.math.BigDecimal("0.00"));
            tipoClienteRepository.save(tc);
        }
    }
}
