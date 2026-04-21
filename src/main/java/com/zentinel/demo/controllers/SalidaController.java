package com.zentinel.demo.controllers;

import com.zentinel.demo.models.*;
import com.zentinel.demo.services.*;
import com.zentinel.demo.repositories.UsuarioRepository;
import com.zentinel.demo.repositories.AreaRepository;
import com.zentinel.demo.repositories.ClienteRepository;
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
import org.springframework.transaction.annotation.Transactional;

@Controller
@RequestMapping("/salidas")
public class SalidaController {

    private final SalidaService salidaService;
    private final ProductoService productoService;
    private final AlmacenService almacenService;
    private final UsuarioRepository usuarioRepository;
    private final AreaRepository areaRepository;
    private final ClienteRepository clienteRepository;
    private final com.zentinel.demo.repositories.InventarioRepository inventarioRepository;

    public SalidaController(SalidaService salidaService,
            ProductoService productoService,
            AlmacenService almacenService,
            UsuarioRepository usuarioRepository,
            AreaRepository areaRepository,
            ClienteRepository clienteRepository,
            com.zentinel.demo.repositories.InventarioRepository inventarioRepository) {
        this.salidaService = salidaService;
        this.productoService = productoService;
        this.almacenService = almacenService;
        this.usuarioRepository = usuarioRepository;
        this.areaRepository = areaRepository;
        this.clienteRepository = clienteRepository;
        this.inventarioRepository = inventarioRepository;
    }

    @GetMapping
    public String listarSalidas(Model model, Principal principal, jakarta.servlet.http.HttpSession session) {
        Integer empresaId = com.zentinel.demo.security.TenantContext.getCurrentEmpresaId(session);
        if (empresaId != null) {
            Usuario currentUser = usuarioRepository.findById(principal.getName()).orElse(null);
            List<Salida> salidas = salidaService.findByEmpresaId(empresaId);
            if (currentUser != null && "MOSTRADOR".equals(currentUser.getRol())) {
                List<Integer> almacenIds = currentUser.getAlmacenes().stream().map(Almacen::getId).collect(Collectors.toList());
                salidas = salidas.stream()
                        .filter(s -> s.getAlmacenOrigen() != null && almacenIds.contains(s.getAlmacenOrigen().getId()))
                        .collect(Collectors.toList());
            }
            model.addAttribute("listaSalidas", salidas);
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
        model.addAttribute("almacenes", almacenService.findByUser(currentUser));
        model.addAttribute("clientes", clienteRepository.findByEmpresa_Id(empresaId));
        model.addAttribute("usuarioActual", currentUser);

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
                map.put("aceptaDecimales", inv.getProducto().getAceptaDecimales());
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
            @RequestParam(value = "clienteId", required = false) Integer clienteId,
            Principal principal,
            jakarta.servlet.http.HttpSession session) {

        // Asignar cliente si viene
        if (clienteId != null) {
            clienteRepository.findById(clienteId).ifPresent(c -> {
                salida.setNombreSolicitante(c.getNombre());
                salida.setTipoCliente(c.getTipoCliente() != null ? c.getTipoCliente().getNombre() : null);
            });
        }

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

    @GetMapping("/facturacion")
    public String listarFacturas(Model model, jakarta.servlet.http.HttpSession session) {
        Integer empresaId = com.zentinel.demo.security.TenantContext.getCurrentEmpresaId(session);
        List<Salida> todas = salidaService.findByEmpresaId(empresaId);
        
        // Agrupar por numReporte para mostrar resúmenes de facturas/reportes
        Map<String, List<Salida>> agrupadas = todas.stream()
            .filter(s -> s.getNumReporte() != null && !s.getNumReporte().isEmpty())
            .collect(Collectors.groupingBy(Salida::getNumReporte));

        List<Map<String, Object>> facturas = new ArrayList<>();
        for (Map.Entry<String, List<Salida>> entry : agrupadas.entrySet()) {
            Map<String, Object> factura = new HashMap<>();
            List<Salida> items = entry.getValue();
            factura.put("numReporte", entry.getKey());
            factura.put("itemsCount", items.size());
            factura.put("total", items.stream().map(v -> v.getTotal() != null ? v.getTotal() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add));
            factura.put("fecha", items.get(0).getFecha());
            factura.put("facturado", items.stream().anyMatch(Salida::getFacturado));
            facturas.add(factura);
        }

        // Ordenar por fecha descendente
        facturas.sort((a, b) -> ((java.time.LocalDateTime)b.get("fecha")).compareTo((java.time.LocalDateTime)a.get("fecha")));

        model.addAttribute("facturas", facturas);
        return "salidas/facturas_lista";
    }

    @GetMapping("/facturacion/buscar")
    public String busquedaFacturacion(Model model) {
        return "salidas/facturacion_busqueda";
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
        if (salida == null || Boolean.TRUE.equals(salida.getFacturado())) {
            return "redirect:/salidas?error=facturado";
        }
        model.addAttribute("salida", salida);
        return "salidas/form_edicion";
    }

    @PostMapping("/actualizar/{folio}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_EMPRESA', 'ADMIN')")
    @Transactional
    public String actualizarSalida(@PathVariable Integer folio, 
                                   @RequestParam Map<String, String> params) {
        Salida salida = salidaService.findByFolio(folio);
        if (salida == null || Boolean.TRUE.equals(salida.getFacturado())) {
            return "redirect:/salidas";
        }

        // 1. Actualizar metadatos básicos
        salida.setNombreSolicitante(params.get("nombreSolicitante"));
        salida.setTipoCliente(params.get("tipoCliente"));
        salida.setDepartamento(params.get("departamento"));
        salida.setNumReporte(params.get("numReporte"));
        salida.setNumPresupuesto(params.get("numPresupuesto"));
        salida.setDescripcion(params.get("descripcion"));

        // 2. Procesar Detalles y Reconciliar Inventario
        List<SalidaDetalle> detallesActuales = new ArrayList<>(salida.getDetalles());
        List<Integer> idsEnForm = new ArrayList<>();
        
        int totalRows = Integer.parseInt(params.getOrDefault("totalDetalles", "0"));
        BigDecimal nuevoSubtotal = BigDecimal.ZERO;

        for (int i = 0; i < totalRows; i++) {
            String sid = params.get("detalleId_" + i);
            String sku = params.get("productoSku_" + i);
            BigDecimal nuevaCant = new BigDecimal(params.get("cantidad_" + i));
            BigDecimal nuevoPrecio = new BigDecimal(params.get("precio_" + i));
            
            SalidaDetalle det = null;
            if (sid != null && !sid.isEmpty()) {
                Integer id = Integer.parseInt(sid);
                idsEnForm.add(id);
                det = detallesActuales.stream().filter(d -> d.getId().equals(id)).findFirst().orElse(null);
            }

            if (det != null) {
                // Reconciliar Inventario: (Anterior - Nueva)
                // Si nueva > anterior, restamos más. Si nueva < anterior, devolvemos.
                BigDecimal diferencia = nuevaCant.subtract(det.getCantidadEntregada());
                if (diferencia.compareTo(BigDecimal.ZERO) != 0) {
                    Inventario inv = inventarioRepository.findByProductoAndAlmacen(det.getProducto(), salida.getAlmacenOrigen())
                            .orElseThrow(() -> new RuntimeException("Inventario no encontrado para el ajuste"));
                    inv.setCantidad(inv.getCantidad().subtract(diferencia));
                    inventarioRepository.save(inv);
                }
                det.setCantidadEntregada(nuevaCant);
                det.setPrecioUnitario(nuevoPrecio);
                det.setSubtotalLinea(nuevaCant.multiply(nuevoPrecio));
                nuevoSubtotal = nuevoSubtotal.add(det.getSubtotalLinea());
            }
        }

        // 3. Manejar eliminaciones
        for (SalidaDetalle detOrig : detallesActuales) {
            if (!idsEnForm.contains(detOrig.getId())) {
                // Devolver todo al inventario
                Inventario inv = inventarioRepository.findByProductoAndAlmacen(detOrig.getProducto(), salida.getAlmacenOrigen())
                        .orElseThrow(() -> new RuntimeException("Inventario no encontrado para la devolución"));
                inv.setCantidad(inv.getCantidad().add(detOrig.getCantidadEntregada()));
                inventarioRepository.save(inv);
                salida.getDetalles().remove(detOrig);
                // JPA se encargará de borrar el registro si tiene orphanRemoval=true, sino hay que borrarlo manual
            }
        }

        // 4. Actualizar Totales del Vale
        salida.setSubtotal(nuevoSubtotal);
        salida.setIva(nuevoSubtotal.multiply(new BigDecimal("0.16")));
        salida.setTotal(nuevoSubtotal.add(salida.getIva()));

        salidaService.save(salida);
        return "redirect:/salidas/detalle/" + folio;
    }
}
