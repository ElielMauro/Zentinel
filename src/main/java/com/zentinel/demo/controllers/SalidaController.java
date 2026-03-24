package com.zentinel.demo.controllers;

import com.zentinel.demo.models.*;
import com.zentinel.demo.services.*;
import com.zentinel.demo.repositories.UsuarioRepository;
import com.zentinel.demo.repositories.AreaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@Controller
@RequestMapping("/salidas")
public class SalidaController {

    private final SalidaService salidaService;
    private final ProductoService productoService;
    private final AlmacenService almacenService;
    private final UsuarioRepository usuarioRepository;
    private final AreaRepository areaRepository;

    public SalidaController(SalidaService salidaService,
            ProductoService productoService,
            AlmacenService almacenService,
            UsuarioRepository usuarioRepository,
            AreaRepository areaRepository) {
        this.salidaService = salidaService;
        this.productoService = productoService;
        this.almacenService = almacenService;
        this.usuarioRepository = usuarioRepository;
        this.areaRepository = areaRepository;
    }

    @GetMapping
    public String listarSalidas(Model model) {
        model.addAttribute("listaSalidas", salidaService.findAll());
        return "salidas";
    }

    @GetMapping("/nueva")
    public String nuevaSalida(Model model, Principal principal, jakarta.servlet.http.HttpSession session) {
        Usuario currentUser = usuarioRepository.findById(principal.getName()).orElse(null);
        Almacen activeAlmacen = (Almacen) session.getAttribute("activeAlmacen");

        model.addAttribute("salida", new Salida());
        model.addAttribute("productos", productoService.findAll());
        model.addAttribute("almacenes", almacenService.findByUser(currentUser));
        model.addAttribute("areas", areaRepository.findAll());
        model.addAttribute("usuarios", usuarioRepository.findAll());
        // El que atiende puede ser cualquier usuario con rol ADMIN o MOSTRADOR
        model.addAttribute("usuariosAtendio", usuarioRepository.findAll()); 

        if (activeAlmacen != null) {
            List<Inventario> inventarioFisico = salidaService.getInventarioByAlmacen(activeAlmacen);
            
            // Crear una lista de objetos simples para el mapa de inventario
            List<Map<String, Object>> inventarioDisponible = inventarioFisico.stream().map(inv -> {
                Map<String, Object> map = new HashMap<>();
                BigDecimal comprometido = salidaService.getStockComprometido(inv.getProducto(), activeAlmacen);
                BigDecimal disponible = inv.getCantidad().subtract(comprometido);
                
                map.put("sku", inv.getProducto().getSku());
                map.put("nombre", inv.getProducto().getNombre());
                map.put("precio", inv.getProducto().getPrecioUnitario());
                map.put("cantidad", disponible);
                return map;
            }).collect(Collectors.toList());

            model.addAttribute("inventarioActivo", inventarioDisponible);
        }

        return "salidas/form";
    }

    @PostMapping("/guardar")
    public String guardarSalida(@ModelAttribute Salida salida,
            @RequestParam("productoId") List<String> skus,
            @RequestParam("cantidad") List<java.math.BigDecimal> cantidades,
            @RequestParam(value = "precioUnitario", required = false) List<java.math.BigDecimal> precios,
            Principal principal) {

        // Si no se asignó alguien que atendió en el modelo, usar el usuario logueado
        if (salida.getUsuarioAtendio() == null) {
            Usuario currentUser = usuarioRepository.findById(principal.getName()).orElse(null);
            salida.setUsuarioAtendio(currentUser);
        }

        List<SalidaDetalle> detalles = new ArrayList<>();
        for (int i = 0; i < skus.size(); i++) {
            String sku = skus.get(i);
            if (sku == null || sku.trim().isEmpty())
                continue;

            SalidaDetalle det = new SalidaDetalle();
            Producto p = productoService.findBySku(sku);
            if (p == null)
                continue;

            det.setProducto(p);
            det.setCantidadSolicitada(cantidades.get(i));
            det.setCantidadEntregada(cantidades.get(i));
            
            // Usar el precio enviado (si existe) o el del catálogo
            if (precios != null && i < precios.size() && precios.get(i) != null) {
                det.setPrecioUnitario(precios.get(i));
            } else {
                det.setPrecioUnitario(p.getPrecioUnitario());
            }
            detalles.add(det);
        }

        if (detalles.isEmpty()) {
            return "redirect:/salidas/nueva?error=no_items";
        }

        Salida guardada = salidaService.registrarSalida(salida, detalles);
        return "redirect:/salidas/reporte/" + guardada.getFolio();
    }

    @GetMapping("/detalle/{folio}")
    public String verDetalle(@PathVariable Integer folio, Model model) {
        Salida salida = salidaService.findByFolio(folio);
        if (salida == null)
            return "redirect:/salidas";
        model.addAttribute("salida", salida);
        return "salidas/detalle";
    }

    @GetMapping("/reporte/{folio}")
    public String verReporte(@PathVariable Integer folio, Model model) {
        Salida salida = salidaService.findByFolio(folio);
        if (salida == null)
            return "redirect:/salidas";
        model.addAttribute("salida", salida);
        return "salidas/reporte";
    }
}
