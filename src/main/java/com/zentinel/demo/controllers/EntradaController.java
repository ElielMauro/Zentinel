package com.zentinel.demo.controllers;

import com.zentinel.demo.models.*;
import com.zentinel.demo.services.*;
import com.zentinel.demo.repositories.ProveedorRepository;
import com.zentinel.demo.repositories.UsuarioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Controller
@RequestMapping("/entradas")
public class EntradaController {

    private final EntradaService entradaService;
    private final ProveedorRepository proveedorRepository;
    private final AlmacenService almacenService;
    private final ProductoService productoService;
    private final UsuarioRepository usuarioRepository;
    private final SalidaService salidaService;

    public EntradaController(EntradaService entradaService,
            ProveedorRepository proveedorRepository,
            AlmacenService almacenService,
            ProductoService productoService,
            UsuarioRepository usuarioRepository,
            SalidaService salidaService) {
        this.entradaService = entradaService;
        this.proveedorRepository = proveedorRepository;
        this.almacenService = almacenService;
        this.productoService = productoService;
        this.usuarioRepository = usuarioRepository;
        this.salidaService = salidaService;
    }

    @GetMapping
    public String list(Model model, jakarta.servlet.http.HttpSession session) {
        Integer empresaId = com.zentinel.demo.security.TenantContext.getCurrentEmpresaId(session);
        if (empresaId != null) {
            model.addAttribute("listaEntradas", entradaService.findByEmpresaId(empresaId));
        } else {
            model.addAttribute("listaEntradas", new ArrayList<>());
        }
        return "entradas";
    }

    @GetMapping("/nueva")
    public String form(Model model, Principal principal, jakarta.servlet.http.HttpSession session) {
        Integer empresaId = com.zentinel.demo.security.TenantContext.getCurrentEmpresaId(session);
        if (empresaId == null) return "redirect:/login";

        Usuario currentUser = usuarioRepository.findById(principal.getName()).orElse(null);
        Almacen activeAlmacen = com.zentinel.demo.security.TenantContext.getActiveAlmacen(session);

        Entrada entrada = new Entrada();
        entrada.setFechaFactura(LocalDateTime.now());
        model.addAttribute("entrada", entrada);
        model.addAttribute("proveedores", proveedorRepository.findAll()); // Debería filtrarse por empresa también si hay tabla proveedores
        model.addAttribute("almacenes", almacenService.findByUser(currentUser)); // findByUser ya filtra por empresa internamente si es ADMIN_EMPRESA
        model.addAttribute("productos", productoService.findByEmpresaId(empresaId));

        if (activeAlmacen != null) {
            model.addAttribute("inventarioActivo", salidaService.getInventarioByAlmacen(activeAlmacen));
        }

        return "entradas/form";
    }

    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable Integer id, Model model) {
        Entrada entrada = entradaService.findById(id);
        if (entrada == null)
            return "redirect:/entradas";
        model.addAttribute("entrada", entrada);
        return "entradas/detalle";
    }

    @PostMapping("/guardar")
    public String save(@ModelAttribute Entrada entrada,
            @RequestParam("productoSku") List<String> skus,
            @RequestParam("cantidad") List<java.math.BigDecimal> cantidades,
            @RequestParam("precio") List<java.math.BigDecimal> precios,
            Principal principal,
            jakarta.servlet.http.HttpSession session) {

        Integer empresaId = com.zentinel.demo.security.TenantContext.getCurrentEmpresaId(session);
        if (empresaId != null) {
            com.zentinel.demo.models.Empresa emp = new com.zentinel.demo.models.Empresa();
            emp.setId(empresaId);
            entrada.setEmpresa(emp);
        }

        List<EntradaDetalle> detalles = new ArrayList<>();
        for (int i = 0; i < skus.size(); i++) {
            String sku = skus.get(i);
            if (sku == null || sku.trim().isEmpty())
                continue;

            Producto p = productoService.findBySku(sku);
            if (p == null)
                continue;

            EntradaDetalle det = new EntradaDetalle();
            det.setProducto(p);
            det.setCantidad(cantidades.get(i));
            det.setPrecioUnitario(precios.get(i));
            detalles.add(det);
        }

        if (detalles.isEmpty()) {
            return "redirect:/entradas/nueva?error=no_items";
        }

        entradaService.registrarEntrada(entrada, detalles);
        return "redirect:/entradas";
    }
}
