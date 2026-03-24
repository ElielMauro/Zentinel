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
    public String list(Model model) {
        model.addAttribute("listaEntradas", entradaService.findAll());
        return "entradas";
    }

    @GetMapping("/nueva")
    public String form(Model model, Principal principal, jakarta.servlet.http.HttpSession session) {
        Usuario currentUser = usuarioRepository.findById(principal.getName()).orElse(null);
        Almacen activeAlmacen = (Almacen) session.getAttribute("activeAlmacen");

        Entrada entrada = new Entrada();
        entrada.setFechaFactura(LocalDateTime.now());
        model.addAttribute("entrada", entrada);
        model.addAttribute("proveedores", proveedorRepository.findAll());
        model.addAttribute("almacenes", almacenService.findByUser(currentUser));
        model.addAttribute("productos", productoService.findAll());

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
            Principal principal) {

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
