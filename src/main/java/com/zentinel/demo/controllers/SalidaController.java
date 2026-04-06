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
    public String listarSalidas(Model model, jakarta.servlet.http.HttpSession session) {
        Integer empresaId = com.zentinel.demo.security.TenantContext.getCurrentEmpresaId(session);
        if (empresaId != null) {
            model.addAttribute("listaSalidas", salidaService.findByEmpresaId(empresaId));
        } else {
            model.addAttribute("listaSalidas", new ArrayList<>());
        }
        return "salidas";
    }

    @GetMapping("/nueva")
    public String nuevaSalida(Model model, Principal principal, jakarta.servlet.http.HttpSession session) {
        Integer empresaId = com.zentinel.demo.security.TenantContext.getCurrentEmpresaId(session);
        if (empresaId == null) return "redirect:/login";

        Usuario currentUser = usuarioRepository.findById(principal.getName()).orElse(null);
        Almacen activeAlmacen = com.zentinel.demo.security.TenantContext.getActiveAlmacen(session);

        model.addAttribute("salida", new Salida());
        model.addAttribute("productos", productoService.findByEmpresaId(empresaId));
        model.addAttribute("almacenes", almacenService.findByUser(currentUser)); // findByUser ya filtra por empresa internamente si es ADMIN_EMPRESA
        model.addAttribute("areas", areaRepository.findAll()); // Debería filtrarse por empresa también si hay tabla áreas
        model.addAttribute("usuarios", usuarioRepository.findByEmpresa_Id(empresaId));
        model.addAttribute("usuariosAtendio", usuarioRepository.findByEmpresa_Id(empresaId)); 

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
            Principal principal,
            jakarta.servlet.http.HttpSession session) {

        Integer empresaId = com.zentinel.demo.security.TenantContext.getCurrentEmpresaId(session);
        if (empresaId != null) {
            com.zentinel.demo.models.Empresa emp = new com.zentinel.demo.models.Empresa();
            emp.setId(empresaId);
            salida.setEmpresa(emp);
        }

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

    @GetMapping("/facturacion/{numReporte}")
    public String verFacturacionAgrupada(@PathVariable String numReporte, Model model, jakarta.servlet.http.HttpSession session) {
        Integer empresaId = com.zentinel.demo.security.TenantContext.getCurrentEmpresaId(session);
        List<Salida> salidas = salidaService.findByEmpresaId(empresaId).stream()
                .filter(s -> numReporte.equals(s.getNumReporte()))
                .collect(Collectors.toList());

        if (salidas.isEmpty()) return "redirect:/salidas";

        BigDecimal totalSuma = salidas.stream().map(Salida::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal subtotalSuma = salidas.stream().map(Salida::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal ivaSuma = salidas.stream().map(Salida::getIva).reduce(BigDecimal.ZERO, BigDecimal::add);

        boolean yaFacturado = salidas.stream().anyMatch(Salida::getFacturado);

        model.addAttribute("salidas", salidas);
        model.addAttribute("numReporte", numReporte);
        model.addAttribute("totalSuma", totalSuma);
        model.addAttribute("subtotalSuma", subtotalSuma);
        model.addAttribute("ivaSuma", ivaSuma);
        model.addAttribute("yaFacturado", yaFacturado);

        return "salidas/facturacion";
    }

    @PostMapping("/facturar/{numReporte}")
    public String procesarFacturacion(@PathVariable String numReporte, jakarta.servlet.http.HttpSession session, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Integer empresaId = com.zentinel.demo.security.TenantContext.getCurrentEmpresaId(session);
        List<Salida> salidas = salidaService.findByEmpresaId(empresaId).stream()
                .filter(s -> numReporte.equals(s.getNumReporte()))
                .collect(Collectors.toList());

        for(Salida s : salidas) {
            s.setFacturado(true);
            salidaService.save(s);
        }

        redirectAttributes.addFlashAttribute("mensaje", "Reporte " + numReporte + " ha sido facturado y cerrado. No se podrán añadir más salidas a este reporte.");
        return "redirect:/salidas/facturacion/" + numReporte;
    }

    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_EMPRESA', 'ADMIN')")
    @GetMapping("/editar/{folio}")
    public String editarSalida(@PathVariable Integer folio, Model model) {
        Salida salida = salidaService.findByFolio(folio);
        if (salida == null || salida.getFacturado()) {
            return "redirect:/salidas?error=facturado";
        }
        model.addAttribute("salida", salida);
        return "salidas/form_edicion"; // Esta sería una vista especial solo para editar metadatos
    }
}
