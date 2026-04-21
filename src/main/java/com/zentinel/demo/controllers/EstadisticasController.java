package com.zentinel.demo.controllers;

import com.zentinel.demo.models.*;
import com.zentinel.demo.repositories.*;
import com.zentinel.demo.security.TenantContext;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/estadisticas")
public class EstadisticasController {

    private final SalidaRepository salidaRepository;
    private final SalidaDetalleRepository salidaDetalleRepository;
    private final EntradaRepository entradaRepository;
    private final InventarioRepository inventarioRepository;
    private final ProductoRepository productoRepository;

    public EstadisticasController(SalidaRepository salidaRepository,
                                   SalidaDetalleRepository salidaDetalleRepository,
                                   EntradaRepository entradaRepository,
                                   InventarioRepository inventarioRepository,
                                   ProductoRepository productoRepository) {
        this.salidaRepository = salidaRepository;
        this.salidaDetalleRepository = salidaDetalleRepository;
        this.entradaRepository = entradaRepository;
        this.inventarioRepository = inventarioRepository;
        this.productoRepository = productoRepository;
    }

    @GetMapping
    public String dashboard(Model model, HttpSession session) {
        Integer empresaId = TenantContext.getCurrentEmpresaId(session);
        if (empresaId == null) return "redirect:/login";

        LocalDateTime startOfMonth = LocalDateTime.now()
                .with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);

        // ─── KPIs Básicos ────────────────────────────────────────────
        long entradasMes = entradaRepository.countByEmpresa_IdAndFechaRecepcionAfter(empresaId, startOfMonth);
        long salidasMes  = salidaRepository.countByEmpresa_IdAndFechaAfter(empresaId, startOfMonth);

        // Salidas monetarias del mes
        List<Salida> todasSalidas = salidaRepository.findByEmpresa_Id(empresaId);
        BigDecimal totalSalidasMesMonto = todasSalidas.stream()
                .filter(s -> !s.getCancelado() && s.getFecha() != null && s.getFecha().isAfter(startOfMonth))
                .map(s -> s.getTotal() != null ? s.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Valor total del inventario: suma(cantidad * precioUnitario) por empresa
        List<Inventario> inventarios = inventarioRepository.findByAlmacen_Empresa_Id(empresaId);
        BigDecimal valorInventario = inventarios.stream()
                .map(i -> {
                    BigDecimal precio = i.getProducto() != null && i.getProducto().getPrecioUnitario() != null
                            ? i.getProducto().getPrecioUnitario() : BigDecimal.ZERO;
                    return i.getCantidad() != null ? i.getCantidad().multiply(precio) : BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ─── Top 25 Productos más Salidos ─────────────────────────────
        List<Object[]> top25Raw = salidaDetalleRepository.findTopProductosDispatched(
                empresaId, PageRequest.of(0, 25));
        List<Map<String, Object>> top25 = new ArrayList<>();
        for (Object[] row : top25Raw) {
            Map<String, Object> item = new LinkedHashMap<>();
            Producto p = (Producto) row[0];
            item.put("sku", p.getSku());
            item.put("nombre", p.getNombre());
            item.put("cantidad", row[1]);
            top25.add(item);
        }

        // ─── Sum Monetaria por Departamento (mes actual) ──────────────
        Map<String, BigDecimal> gastosPorDepto = todasSalidas.stream()
                .filter(s -> !s.getCancelado() && s.getFecha() != null && s.getFecha().isAfter(startOfMonth))
                .collect(Collectors.groupingBy(
                        s -> s.getDepartamento() != null ? s.getDepartamento() : "Sin Departamento",
                        Collectors.reducing(BigDecimal.ZERO,
                                s -> s.getTotal() != null ? s.getTotal() : BigDecimal.ZERO,
                                BigDecimal::add)
                ));
        // Ordenar de mayor a menor
        Map<String, BigDecimal> gastosPorDeptoOrdenado = gastosPorDepto.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        // Departamento con mayor consumo
        String topDepto = gastosPorDeptoOrdenado.isEmpty() ? "N/A" :
                gastosPorDeptoOrdenado.entrySet().iterator().next().getKey();

        // ─── Stock Crítico (según minimoStock de producto) ─────────────
        List<Map<String, Object>> stockCritico = new ArrayList<>();
        for (Inventario inv : inventarios) {
            Producto prod = inv.getProducto();
            if (prod == null) continue;
            Integer minimo = prod.getMinimoStock();
            if (minimo != null && inv.getCantidad() != null
                    && inv.getCantidad().compareTo(new BigDecimal(minimo)) <= 0) {
                Map<String, Object> alerta = new LinkedHashMap<>();
                alerta.put("sku", prod.getSku());
                alerta.put("nombre", prod.getNombre());
                alerta.put("cantidadActual", inv.getCantidad());
                alerta.put("minimoStock", minimo);
                alerta.put("almacen", inv.getAlmacen() != null ? inv.getAlmacen().getNombre() : "—");
                stockCritico.add(alerta);
            }
        }

        // ─── Datos para Gráficas (JSON strings para Chart.js) ─────────
        // Gráfica departamentos: labels y data
        List<String> deptoLabels = new ArrayList<>(gastosPorDeptoOrdenado.keySet());
        List<BigDecimal> deptoData = new ArrayList<>(gastosPorDeptoOrdenado.values());

        // Gráfica top25: labels y data
        List<String> top25Labels = top25.stream().map(m -> (String) m.get("nombre")).collect(Collectors.toList());
        List<Object> top25Data   = top25.stream().map(m -> m.get("cantidad")).collect(Collectors.toList());

        // ─── Recomendaciones de Compra por Temporada ──────────────────
        int mesActual = LocalDateTime.now().getMonthValue();
        String estacion = getEstacion(mesActual);
        String descEstacion = getDescripcionEstacion(estacion, mesActual);

        List<Object[]> recoRaw = salidaDetalleRepository.findRecomendacionesPorTemporada(
                empresaId, mesActual, PageRequest.of(0, 10));
        List<Map<String, Object>> recomendaciones = new ArrayList<>();
        for (Object[] row : recoRaw) {
            Map<String, Object> item = new LinkedHashMap<>();
            Producto p = (Producto) row[0];
            Number cant = (Number) row[1];
            item.put("sku", p.getSku());
            item.put("nombre", p.getNombre());
            item.put("cantidad", cant);
            // Calcular prioridad: Alta > 50, Media > 20, Baja
            String prioridad;
            if (cant.doubleValue() > 50) prioridad = "Alta";
            else if (cant.doubleValue() > 20) prioridad = "Media";
            else prioridad = "Baja";
            item.put("prioridad", prioridad);
            recomendaciones.add(item);
        }

        model.addAttribute("entradasMes", entradasMes);
        model.addAttribute("salidasMes", salidasMes);
        model.addAttribute("totalSalidasMesMonto", totalSalidasMesMonto);
        model.addAttribute("valorInventario", valorInventario);
        model.addAttribute("top25", top25);
        model.addAttribute("gastosPorDepto", gastosPorDeptoOrdenado);
        model.addAttribute("topDepto", topDepto);
        model.addAttribute("stockCritico", stockCritico);
        model.addAttribute("deptoLabels", deptoLabels);
        model.addAttribute("deptoData", deptoData);
        model.addAttribute("top25Labels", top25Labels);
        model.addAttribute("top25Data", top25Data);
        model.addAttribute("recomendaciones", recomendaciones);
        model.addAttribute("estacion", estacion);
        model.addAttribute("descEstacion", descEstacion);
        model.addAttribute("mesActualNombre", LocalDateTime.now().getMonth().getDisplayName(java.time.format.TextStyle.FULL, new java.util.Locale("es", "MX")));

        return "estadisticas";
    }

    // ─── Helpers de Estación ──────────────────────────────────────────────
    private String getEstacion(int mes) {
        if (mes >= 3 && mes <= 5) return "Primavera";
        if (mes >= 6 && mes <= 8) return "Verano";
        if (mes >= 9 && mes <= 11) return "Otoño";
        return "Invierno";
    }

    private String getDescripcionEstacion(String estacion, int mes) {
        return switch (estacion) {
            case "Primavera" -> "Temporada de mayor actividad de obra y construcción. Se recomienda surtir materiales de acabado, pintura y herramientas.";
            case "Verano"    -> "Temporada alta en proyectos exteriores e hidráulicos. Prioriza materiales de plomería, tuberías y selladores.";
            case "Otoño"     -> "Inicio de temporada de mantenimiento preventivo. Enfócate en refacciones y materiales de cierre de proyectos.";
            case "Invierno"  -> "Temporada de proyectos interiores y mantenimiento de instalaciones. Surtir materiales eléctricos y de aislamiento.";
            default          -> "Analiza los datos históricos para tomar decisiones de surtido.";
        };
    }

    // ─── Endpoint AJAX para buscar uso de un producto por departamentos ────
    @GetMapping("/producto/{sku}")
    @ResponseBody
    public Map<String, Object> usoPorProducto(@PathVariable String sku, HttpSession session) {
        Integer empresaId = TenantContext.getCurrentEmpresaId(session);
        Map<String, Object> result = new LinkedHashMap<>();

        if (empresaId == null) {
            result.put("error", "Sin empresa en sesión");
            return result;
        }

        List<Object[]> rows = salidaDetalleRepository.findDepartamentosByProducto(empresaId, sku);
        List<String> labels = new ArrayList<>();
        List<Object> data   = new ArrayList<>();

        for (Object[] row : rows) {
            labels.add(row[0] != null ? (String) row[0] : "Sin Depto");
            data.add(row[1]);
        }

        Producto p = productoRepository.findById(sku).orElse(null);
        result.put("productoNombre", p != null ? p.getNombre() : sku);
        result.put("labels", labels);
        result.put("data", data);
        return result;
    }
}
