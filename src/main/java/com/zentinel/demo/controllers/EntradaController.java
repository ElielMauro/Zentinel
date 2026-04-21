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
import java.util.Map;
import java.math.BigDecimal;
import org.springframework.transaction.annotation.Transactional;

@Controller
@RequestMapping("/entradas")
public class EntradaController {

    private final EntradaService entradaService;
    private final ProveedorRepository proveedorRepository;
    private final AlmacenService almacenService;
    private final ProductoService productoService;
    private final UsuarioRepository usuarioRepository;
    private final SalidaService salidaService;
    private final com.zentinel.demo.repositories.InventarioRepository inventarioRepository;

    public EntradaController(EntradaService entradaService,
            ProveedorRepository proveedorRepository,
            AlmacenService almacenService,
            ProductoService productoService,
            UsuarioRepository usuarioRepository,
            SalidaService salidaService,
            com.zentinel.demo.repositories.InventarioRepository inventarioRepository) {
        this.entradaService = entradaService;
        this.proveedorRepository = proveedorRepository;
        this.almacenService = almacenService;
        this.productoService = productoService;
        this.usuarioRepository = usuarioRepository;
        this.salidaService = salidaService;
        this.inventarioRepository = inventarioRepository;
    }

    @GetMapping
    public String list(Model model, Principal principal, jakarta.servlet.http.HttpSession session) {
        Integer empresaId = com.zentinel.demo.security.TenantContext.getCurrentEmpresaId(session);
        if (empresaId != null) {
            Usuario currentUser = usuarioRepository.findById(principal.getName()).orElse(null);
            List<Entrada> entradas = entradaService.findByEmpresaId(empresaId);
            if (currentUser != null && "MOSTRADOR".equals(currentUser.getRol())) {
                List<Integer> almacenIds = currentUser.getAlmacenes().stream().map(Almacen::getId).collect(java.util.stream.Collectors.toList());
                entradas = entradas.stream()
                        .filter(e -> e.getAlmacenDestino() != null && almacenIds.contains(e.getAlmacenDestino().getId()))
                        .collect(java.util.stream.Collectors.toList());
            }
            model.addAttribute("listaEntradas", entradas);
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

    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_EMPRESA', 'ADMIN')")
    @GetMapping("/editar/{id}")
    public String editarEntrada(@PathVariable Integer id, Model model) {
        Entrada entrada = entradaService.findById(id);
        if (entrada == null) {
            return "redirect:/entradas";
        }
        model.addAttribute("entrada", entrada);
        return "entradas/form_edicion";
    }

    @PostMapping("/actualizar/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_EMPRESA', 'ADMIN')")
    @Transactional
    public String actualizarEntrada(@PathVariable Integer id, 
                                    @RequestParam Map<String, String> params) {
        Entrada entrada = entradaService.findById(id);
        if (entrada == null) {
            return "redirect:/entradas";
        }

        // 1. Actualizar metadatos
        entrada.setFolioFactura(params.get("folioFactura"));
        entrada.setNumPp(params.get("numPp"));
        entrada.setComentarios(params.get("comentarios"));

        // 2. Procesar Detalles y Reconciliar Inventario
        List<EntradaDetalle> detallesActuales = new ArrayList<>(entrada.getDetalles());
        List<Integer> idsEnForm = new ArrayList<>();
        
        int totalRows = Integer.parseInt(params.getOrDefault("totalDetalles", "0"));
        BigDecimal nuevoSubtotal = BigDecimal.ZERO;

        for (int i = 0; i < totalRows; i++) {
            String did = params.get("detalleId_" + i);
            BigDecimal nuevaCant = new BigDecimal(params.get("cantidad_" + i));
            BigDecimal nuevoPrecio = new BigDecimal(params.get("precio_" + i));
            
            EntradaDetalle det = null;
            if (did != null && !did.isEmpty()) {
                Integer detailId = Integer.parseInt(did);
                idsEnForm.add(detailId);
                det = detallesActuales.stream().filter(d -> d.getId().equals(detailId)).findFirst().orElse(null);
            }

            if (det != null) {
                // Reconciliar Inventario: (Nueva - Anterior)
                // Si nueva > anterior, sumamos más al inventario. Si nueva < anterior, restamos.
                BigDecimal diferencia = nuevaCant.subtract(det.getCantidad());
                if (diferencia.compareTo(BigDecimal.ZERO) != 0) {
                    Inventario inv = inventarioRepository.findByProductoAndAlmacen(det.getProducto(), entrada.getAlmacenDestino())
                            .orElseThrow(() -> new RuntimeException("Inventario no encontrado para el ajuste"));
                    inv.setCantidad(inv.getCantidad().add(diferencia));
                    inventarioRepository.save(inv);
                }
                det.setCantidad(nuevaCant);
                det.setPrecioUnitario(nuevoPrecio);
                det.setSubtotalLinea(nuevaCant.multiply(nuevoPrecio));
                nuevoSubtotal = nuevoSubtotal.add(det.getSubtotalLinea());
            }
        }

        // 3. Manejar eliminaciones
        for (EntradaDetalle detOrig : detallesActuales) {
            if (!idsEnForm.contains(detOrig.getId())) {
                // Restar del inventario lo que se había sumado
                Inventario inv = inventarioRepository.findByProductoAndAlmacen(detOrig.getProducto(), entrada.getAlmacenDestino())
                        .orElseThrow(() -> new RuntimeException("Inventario no encontrado para el ajuste"));
                inv.setCantidad(inv.getCantidad().subtract(detOrig.getCantidad()));
                inventarioRepository.save(inv);
                entrada.getDetalles().remove(detOrig);
            }
        }

        // 4. Actualizar Totales
        entrada.setSubtotal(nuevoSubtotal);
        entrada.setIva(nuevoSubtotal.multiply(new BigDecimal("0.16")));
        entrada.setTotal(nuevoSubtotal.add(entrada.getIva()));

        entradaService.save(entrada);
        return "redirect:/entradas/detalle/" + id;
    }
}
