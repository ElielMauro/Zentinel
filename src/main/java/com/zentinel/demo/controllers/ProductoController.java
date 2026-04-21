package com.zentinel.demo.controllers;

import com.zentinel.demo.models.Producto;
import com.zentinel.demo.services.ProductoService;
import com.zentinel.demo.repositories.TipoProductoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService productoService;
    private final TipoProductoRepository tipoProductoRepository;
    private final com.zentinel.demo.repositories.InventarioRepository inventarioRepository;

    public ProductoController(ProductoService productoService, TipoProductoRepository tipoProductoRepository, com.zentinel.demo.repositories.InventarioRepository inventarioRepository) {
        this.productoService = productoService;
        this.tipoProductoRepository = tipoProductoRepository;
        this.inventarioRepository = inventarioRepository;
    }

    @GetMapping
    public String list(Model model, jakarta.servlet.http.HttpSession session) {
        Integer empresaId = com.zentinel.demo.security.TenantContext.getCurrentEmpresaId(session);
        com.zentinel.demo.models.Almacen activeAlmacen = com.zentinel.demo.security.TenantContext.getActiveAlmacen(session);
        
        System.out.println("DEBUG: Listando productos para empresaId: " + empresaId + ", Almacén Activo: " + (activeAlmacen != null ? activeAlmacen.getNombre() : "Ninguno"));
        
        if (empresaId != null) {
            java.util.List<Producto> productos = productoService.findByEmpresaId(empresaId);
            System.out.println("DEBUG: Productos encontrados para la empresa: " + (productos != null ? productos.size() : "null"));
            
            java.util.List<java.util.Map<String, Object>> listaConStock = new java.util.ArrayList<>();
            
            for (Producto p : productos) {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("producto", p);
                
                java.util.List<com.zentinel.demo.models.Inventario> invs = inventarioRepository.findByProducto(p);
                java.math.BigDecimal totalGlobal = java.math.BigDecimal.ZERO;
                java.math.BigDecimal stockAlmacen = java.math.BigDecimal.ZERO;
                
                if (invs != null) {
                    for (com.zentinel.demo.models.Inventario inv : invs) {
                        if (inv.getCantidad() != null) {
                            totalGlobal = totalGlobal.add(inv.getCantidad());
                            // Comprobar que el inventario tenga un almacén asignado para evitar NPE
                            if (activeAlmacen != null && inv.getAlmacen() != null && inv.getAlmacen().getId().equals(activeAlmacen.getId())) {
                                stockAlmacen = inv.getCantidad();
                            }
                        }
                    }
                }
                
                map.put("totalGlobal", totalGlobal);
                map.put("stockAlmacen", stockAlmacen);
                listaConStock.add(map);
            }
            
            model.addAttribute("listaProductos", listaConStock);
            model.addAttribute("activeAlmacen", activeAlmacen);
        } else {
            System.err.println("DEBUG: No se encontró empresaId en el contexto para el usuario actual.");
            model.addAttribute("listaProductos", new java.util.ArrayList<>());
        }
        return "productos";
    }

    @GetMapping("/nuevo")
    @org.springframework.security.access.prepost.PreAuthorize("!hasRole('MOSTRADOR')")
    public String form(Model model) {
        System.out.println("DEBUG: Entrando a /productos/nuevo");
        try {
            model.addAttribute("producto", new Producto());
            var cats = tipoProductoRepository.findAll();
            System.out.println("DEBUG: Categorias encontradas: " + (cats != null ? cats.size() : "null"));
            model.addAttribute("categorias", cats);
            return "productos/form";
        } catch (Exception e) {
            System.err.println("DEBUG ERROR en /productos/nuevo: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/editar/{sku}")
    @org.springframework.security.access.prepost.PreAuthorize("!hasRole('MOSTRADOR')")
    public String formEdicion(@PathVariable String sku, Model model) {
        Producto p = productoService.findBySku(sku);
        if (p == null) return "redirect:/productos";
        model.addAttribute("producto", p);
        model.addAttribute("categorias", tipoProductoRepository.findAll());
        model.addAttribute("edicion", true);
        return "productos/form";
    }

    @GetMapping("/detalle/{sku}")
    public String detail(@PathVariable String sku, Model model) {
        Producto p = productoService.findBySku(sku);
        if (p == null) return "redirect:/productos";
        model.addAttribute("producto", p);
        java.util.List<com.zentinel.demo.models.Inventario> invs = inventarioRepository.findByProducto(p);
        model.addAttribute("inventarios", invs);
        java.math.BigDecimal totalStock = java.math.BigDecimal.ZERO;
        if(invs != null) {
            for(com.zentinel.demo.models.Inventario i : invs) {
                if(i.getCantidad() != null) {
                    totalStock = totalStock.add(i.getCantidad());
                }
            }
        }
        model.addAttribute("totalStock", totalStock);
        return "productos/detalle";
    }

    @PostMapping("/guardar")
    @org.springframework.security.access.prepost.PreAuthorize("!hasRole('MOSTRADOR')")
    public String save(@ModelAttribute Producto producto, jakarta.servlet.http.HttpSession session) {
        if (producto.getEmpresa() == null) {
            Integer empresaId = com.zentinel.demo.security.TenantContext.getCurrentEmpresaId(session);
            if (empresaId != null) {
                com.zentinel.demo.models.Empresa emp = new com.zentinel.demo.models.Empresa();
                emp.setId(empresaId);
                producto.setEmpresa(emp);
            }
        }
        productoService.save(producto);
        return "redirect:/productos";
    }
}
